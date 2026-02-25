#!/usr/bin/env python3
"""
Task Dispatcher - Automated Task Scheduling Script
===================================================
此腳本由 Task Dispatcher (教導者模式) 根據專案配置自動產出。
所有參數皆為硬編碼，無需任何手動修改。

Usage:
    python dispatcher.py
    
設定排程 (Cron):
    每小時執行一次: 0 * * * * cd /path/to/project && python skills/task-dispatcher/dispatcher.py
"""
import os
import sys
import json
import subprocess

# ============================================================
# 📌 專案配置（由 Task Dispatcher 教導者根據使用者資訊填入）
# ============================================================
AGENT_NAME = "{{AGENT_NAME}}"
BASE_BRANCH = "{{BASE_BRANCH}}"
TRACKER_PATH = ".{{AGENT_NAME}}/tracker.json"
RULES_DIR = ".agents/rules/"
# ============================================================


def run_cmd(cmd, check=True):
    """執行 Shell 指令並回傳結果"""
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if check and result.returncode != 0:
        print(f"Error executing: {cmd}\n{result.stderr}", file=sys.stderr)
        sys.exit(1)
    return result.stdout.strip()


def get_next_task():
    """讀取 tracker.json 尋找下一個 pending 的任務"""
    if not os.path.exists(TRACKER_PATH):
        print(f"❌ Tracker not found at '{TRACKER_PATH}'. Factory might not be scaffolded yet.")
        sys.exit(1)

    with open(TRACKER_PATH, 'r', encoding='utf-8') as f:
        data = json.load(f)

    current_phase_name = data.get("current_phase")

    for phase in data.get("phases", []):
        if phase.get("name") == current_phase_name:
            for task in phase.get("tasks", []):
                if task.get("status") == "pending":
                    if int(task.get("attempts", 0)) >= 5:
                        print(f"🚨 Task {task['id']} has failed {task['attempts']} times. Manual intervention required.")
                        sys.exit(1)
                    return task

            print(f"⏳ All tasks in '{current_phase_name}' are completed or in progress. Waiting for CI to bump phase...")
            sys.exit(0)

    print("✅ All phases completed! Software Factory is idle.")
    sys.exit(0)


def check_branch_lock(task_id):
    """檢查遠端分支互斥鎖 (Branch-as-Lock)"""
    branch_name = f"{AGENT_NAME}/task-{task_id}"
    print(f"🔍 Checking remote lock for branch: {branch_name}...")

    output = run_cmd(f"git ls-remote --heads origin {branch_name}", check=False)

    if output:
        print(f"🔒 Branch '{branch_name}' already exists on remote. Task is locked.")
        sys.exit(0)
    return branch_name


def dispatch_task(task, branch_name):
    """建立分支並產出任務包"""
    task_id = task.get("id")
    spec_ref = task.get("spec_ref")

    print(f"\n🚀 Dispatching task: {task_id} ({task.get('title')})")
    print("-" * 50)

    # 確保最新狀態
    run_cmd("git fetch origin")
    run_cmd(f"git checkout {BASE_BRANCH}")
    run_cmd(f"git pull origin {BASE_BRANCH}")

    # 切換 Feature Branch
    run_cmd(f"git checkout -b {branch_name}")

    # 輸出任務包 (Task Package)
    print(f"✅ Feature branch '{branch_name}' created.")
    print("📦 Task Package:")

    pkg = {
        "task_id": task_id,
        "spec_ref": spec_ref,
        "branch": branch_name,
        "base_branch": BASE_BRANCH,
        "rules_dir": RULES_DIR
    }
    print(json.dumps(pkg, indent=2))

    print(f"\n💡 Next: Feed the Worker Prompt to {AGENT_NAME}.")
    print("-" * 50)


def main():
    print(f"🏭 Task Dispatcher (Agent: {AGENT_NAME}, Branch: {BASE_BRANCH})")
    task = get_next_task()
    branch_name = check_branch_lock(task.get("id"))
    dispatch_task(task, branch_name)


if __name__ == "__main__":
    main()
