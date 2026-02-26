package com.tibame.app_generator.service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.tibame.app_generator.dto.analysis.*;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.model.ProjectAnalysis;
import com.tibame.app_generator.repository.ProjectAnalysisRepository;
import com.tibame.app_generator.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final ProjectRepository projectRepository;
    private final ProjectAnalysisRepository projectAnalysisRepository;
    private final GitService gitService;
    private final MetricsService metricsService;

    @Async
    @Transactional
    public void analyzeProject(UUID projectId) {
        metricsService.incrementAnalysisTotal();
        long startTime = System.currentTimeMillis();
        log.info("Starting analysis for project {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        Path tempWorkspace = null;
        try {
            tempWorkspace = Files.createTempDirectory("analysis-" + projectId);

            Path bareRepoPath = gitService.getBareRepoPath(project.getUser().getId(), projectId);

             try (Git git = Git.cloneRepository()
                    .setURI(bareRepoPath.toUri().toString())
                    .setDirectory(tempWorkspace.toFile())
                    .call()) {
                // Cloned
             }

            AnalysisResultDTO result = parseProject(tempWorkspace);

            ProjectAnalysis analysis = projectAnalysisRepository.findByProjectId(projectId)
                    .orElse(ProjectAnalysis.builder().project(project).build());

            analysis.setAnalysisJson(result);
            projectAnalysisRepository.save(analysis);

            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordAnalysisSuccess(duration);
            log.info("Analysis completed for project {} in {}ms", projectId, duration);

        } catch (Exception e) {
            log.error("Analysis failed for project {}", projectId, e);
            metricsService.incrementAnalysisFailed();
        } finally {
            if (tempWorkspace != null) {
                try {
                    gitService.deleteDirectoryRecursively(tempWorkspace);
                } catch (IOException e) {
                    log.warn("Failed to delete temp workspace", e);
                }
            }
        }
    }

    private AnalysisResultDTO parseProject(Path rootPath) throws IOException {
        ParserConfiguration configuration = new ParserConfiguration();
        JavaParser parser = new JavaParser(configuration);

        List<PackageDTO> packages = new ArrayList<>();
        Map<String, List<ClassDTO>> packageMap = new HashMap<>();

        Files.walk(rootPath)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(file -> {
                    try {
                        ParseResult<CompilationUnit> parseResult = parser.parse(file);
                        if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                            CompilationUnit cu = parseResult.getResult().get();
                            String packageName = cu.getPackageDeclaration()
                                    .map(pd -> pd.getNameAsString())
                                    .orElse("default");

                            List<ClassDTO> classes = cu.getTypes().stream()
                                    .map(this::convertType)
                                    .collect(Collectors.toList());

                            packageMap.computeIfAbsent(packageName, k -> new ArrayList<>()).addAll(classes);
                        }
                    } catch (IOException e) {
                        log.warn("Failed to parse file: {}", file, e);
                    }
                });

        packageMap.forEach((name, classes) -> {
            packages.add(PackageDTO.builder()
                    .name(name)
                    .classes(classes)
                    .build());
        });

        return AnalysisResultDTO.builder().packages(packages).build();
    }

    private ClassDTO convertType(TypeDeclaration<?> type) {
        String typeName = type.getNameAsString();
        String typeKind = "CLASS";
        if (type.isEnumDeclaration()) typeKind = "ENUM";
        else if (type.isClassOrInterfaceDeclaration() && ((ClassOrInterfaceDeclaration) type).isInterface()) typeKind = "INTERFACE";

        List<String> modifiers = type.getModifiers().stream()
                .map(m -> m.getKeyword().asString())
                .collect(Collectors.toList());

        List<FieldDTO> fields = type.getFields().stream()
                .flatMap(fd -> fd.getVariables().stream().map(v -> FieldDTO.builder()
                        .name(v.getNameAsString())
                        .type(v.getTypeAsString())
                        .modifiers(fd.getModifiers().stream().map(m -> m.getKeyword().asString()).collect(Collectors.toList()))
                        .build()))
                .collect(Collectors.toList());

        List<MethodDTO> methods = type.getMethods().stream()
                .map(md -> MethodDTO.builder()
                        .name(md.getNameAsString())
                        .returnType(md.getTypeAsString())
                        .modifiers(md.getModifiers().stream().map(m -> m.getKeyword().asString()).collect(Collectors.toList()))
                        .parameters(md.getParameters().stream().map(p -> p.getTypeAsString() + " " + p.getNameAsString()).collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        // Imports (Dependencies)
        List<String> dependencies = new ArrayList<>();
        type.findCompilationUnit().ifPresent(cu -> {
            cu.getImports().forEach(id -> dependencies.add(id.getNameAsString()));
        });

        return ClassDTO.builder()
                .name(typeName)
                .type(typeKind)
                .modifiers(modifiers)
                .fields(fields)
                .methods(methods)
                .dependencies(dependencies)
                .build();
    }

    public AnalysisResultDTO getAnalysis(UUID projectId) {
        return projectAnalysisRepository.findByProjectId(projectId)
                .map(ProjectAnalysis::getAnalysisJson)
                .orElse(null);
    }
}
