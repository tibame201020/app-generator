#!/usr/bin/env python3
import os
import sys
import json
import glob
import subprocess

def run_cmd(cmd, check=True):
    """執行 Shell 指令並回傳結果"""
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if check and result.returncode != 0:
        print(f"Error executing: {cmd}\n{result.stderr}", file=sys.stderr)
        sys.exit(1)
    return result.stdout.strip()

def find_tracker():
    """尋找隱藏目錄下的 tracker.json"""
    trackers = glob.glob(".*/*tracker.json")
    if not trackers:
        print("❌ No tracker.json found. Factory might not be scaffolded yet.")
        sys.exit(1)
    return trackers[0]

def get_next_task(tracker_path):
    """讀取 tracker.json 尋找下一個 pending 的任務"""
    with open(tracker_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    current_phase_name = data.get("current_phase")
    
    # 動態獲取 Agent Name (通常在建廠時寫入)
    agent_name = data.get("agent_name", "jules")
    
    # 動態獲取 Base Branch (不寫死 main，改由 git 偵測)
    base_branch = run_cmd("git rev-parse --abbrev-ref HEAD", check=False)
    if not base_branch or base_branch.startswith("jules/"):
        # 若當前在 detached HEAD 或 feature branch，嘗試取得預設分支
        base_branch = run_cmd("git config --get init.defaultBranch", check=False) or "main"

    for phase in data.get("phases", []):
        if phase.get("name") == current_phase_name:
            for task in phase.get("tasks", []):
                if task.get("status") == "pending":
                    # 檢查多次失敗鎖死
                    if int(task.get("attempts", 0)) >= 5:
                        print(f"🚨 Task {task['id']} has failed {task['attempts']} times. Manual intervention required.")
                        sys.exit(1)
                    return task, agent_name, base_branch
            
            # 若 current_phase 內沒有 pending 任務，代表可能正在等待 CI 合併
            print(f"⏳ All tasks in '{current_phase_name}' are completed or in progress. Waiting for CI to bump phase...")
            sys.exit(0)
    
    print("✅ All phases completed! Software Factory is idle.")
    sys.exit(0)

def check_branch_lock(agent_name, task_id):
    """檢查遠端分支互斥鎖 (Branch-as-Lock)"""
    branch_name = f"{agent_name}/task-{task_id}"
    print(f"🔍 Checking remote lock for branch: {branch_name}...")
    
    # 這裡假設 origin 是預設遠端
    output = run_cmd(f"git ls-remote --heads origin {branch_name}", check=False)
    
    if output:
        print(f"🔒 Branch {branch_name} already exists on remote. Task is currently locked by another worker or in PR.")
        sys.exit(0)
    return branch_name

def dispatch_task(task, branch_name, base_branch, agent_name):
    """建立分支並產出任務包指引"""
    task_id = task.get("id")
    spec_ref = task.get("spec_ref")
    
    print(f"\n🚀 Dispatching task: {task_id} ({task.get('title')})")
    print("-" * 50)
    
    # 1. 確保最新狀態
    run_cmd("git fetch origin")
    run_cmd(f"git checkout {base_branch}")
    run_cmd(f"git pull origin {base_branch}")
    
    # 2. 切換 Feature Branch
    run_cmd(f"git checkout -b {branch_name}")
    
    # 3. 輸出任務包 (Task Package) 供外層腳本呼叫 Worker
    print(f"✅ Feature branch '{branch_name}' created.")
    print("📦 Task Package ready for Worker:")
    
    pkg = {
        "task_id": task_id,
        "spec_ref": spec_ref,
        "branch": branch_name,
        "base_branch": base_branch,
        "rules_dir": ".agents/rules/"
    }
    print(json.dumps(pkg, indent=2))
    
    print("\n💡 NEXT STEP FOR CRON / AUTOMATION:")
    print("Pass this task package to your Worker Agent API, ensuring it reads the spec_ref and follows AGENT_PROTOCOL.md.")
    print(f"Example: `jules execute --spec {spec_ref} --branch {branch_name}`")
    print("-" * 50)

def main():
    print("🏭 Task Dispatcher (v2 - No-LLM Automated Mode) Started")
    tracker_path = find_tracker()
    
    task, agent_name, base_branch = get_next_task(tracker_path)
    branch_name = check_branch_lock(agent_name, task.get("id"))
    
    dispatch_task(task, branch_name, base_branch, agent_name)

if __name__ == "__main__":
    main()
