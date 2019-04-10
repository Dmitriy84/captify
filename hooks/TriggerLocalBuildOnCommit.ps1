param([String]$folder)
Invoke-WebRequest -Uri http://localhost:8080/git/notifyCommit?url=file://$folder -Method POST