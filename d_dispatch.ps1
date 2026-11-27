$Token = "ghp_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"
$Owner = "ibrahimheseni"
$Repo = "ios_launcher"
$Branch = "main"
$ApiBase = "https://api.github.com"

function Push-File($FilePath) {
    $RelativePath = $FilePath.Substring(("C:\\Users\\ibrah\\.gemini\\antigravity\\scratch\\ios_launcher\\").Length)
    $ContentBytes = [System.IO.File]::ReadAllBytes($FilePath)
    $Base64 = [Convert]::ToBase64String($ContentBytes)
    $Url = "$ApiBase/repos/$Owner/$Repo/contents/$RelativePath"
    $Body = @{
        message = "Add $RelativePath"
        content = $Base64
        branch  = $Branch
    } | ConvertTo-Json -Depth 10
    try {
        Invoke-RestMethod -Headers @{Authorization = "token $Token"; "User-Agent" = "PowerShell"} -Uri $Url -Method Put -Body $Body -ContentType "application/json" | Out-Null
        Write-Host "✅ Pushed $RelativePath"
    } catch {
        $err = $_.Exception.Message
        Write-Host "❌ Failed $RelativePath: $err"
    }
}

# Push all files
Get-ChildItem -Recurse -File -Path "C:\Users\ibrah\.gemini\antigravity\scratch\ios_launcher" | ForEach-Object { Push-File $_.FullName }

# Trigger GitHub Actions workflow (build-apk.yml)
$WorkflowFile = "build-apk.yml"
$DispatchUrl = "$ApiBase/repos/$Owner/$Repo/actions/workflows/$WorkflowFile/dispatches"
$DispatchBody = @{ ref = $Branch } | ConvertTo-Json
try {
    Invoke-RestMethod -Headers @{Authorization = "token $Token"; "User-Agent" = "PowerShell"} -Uri $DispatchUrl -Method Post -Body $DispatchBody -ContentType "application/json"
    Write-Host "✅ Workflow dispatch sent"
} catch {
    Write-Host "❌ Workflow dispatch failed: $($_.Exception.Message)"
}

# Get latest workflow run ID
$RunsUrl = "$ApiBase/repos/$Owner/$Repo/actions/workflows/$WorkflowFile/runs?branch=$Branch&event=workflow_dispatch&per_page=1"
try {
    $runs = Invoke-RestMethod -Headers @{Authorization = "token $Token"; "User-Agent" = "PowerShell"} -Uri $RunsUrl -Method Get
    if ($runs.total_count -gt 0) {
        $runId = $runs.workflow_runs[0].id
        $runUrl = "https://github.com/$Owner/$Repo/actions/runs/$runId"
        Write-Host "🟢 Latest run ID: $runId"
        Write-Host "Run URL: $runUrl"
    } else {
        Write-Host "⚠️ No recent runs found"
    }
} catch {
    Write-Host "❌ Could not retrieve runs: $($_.Exception.Message)"
}
