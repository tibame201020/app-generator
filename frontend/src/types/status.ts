export type ImportStatus = 'PENDING' | 'CLONING' | 'SUCCESS' | 'FAILED';
export type AnalysisStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED';

export interface AnalysisMetadataDTO {
    fileCount: number;
    classCount: number;
    methodCount: number;
    durationMs: number;
    lastAnalysisTime: string; // ISO Date string
}

export interface ProjectOperationLogDTO {
    id: string;
    operationType: string;
    status: string;
    message: string;
    timestamp: string; // ISO Date string
}

export interface ProjectStatusDTO {
    projectId: string;
    importStatus: ImportStatus;
    importStartTime?: string;
    importEndTime?: string;
    importFailureReason?: string;

    analysisStatus: AnalysisStatus;
    analysisStartTime?: string;
    analysisEndTime?: string;
    analysisFailureReason?: string;

    metadata?: AnalysisMetadataDTO;
    recentLogs: ProjectOperationLogDTO[];
}
