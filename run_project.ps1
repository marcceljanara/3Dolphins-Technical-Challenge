$mavenZip = Join-Path -Path $pwd -ChildPath "maven.zip"
$mavenDir = Join-Path -Path $pwd -ChildPath ".maven"
$mvnCmd = Join-Path -Path $mavenDir -ChildPath "apache-maven-3.9.6\bin\mvn.cmd"

if (-not (Test-Path $mvnCmd)) {
    Write-Host "Maven not found locally. Downloading Apache Maven 3.9.6..." -ForegroundColor Cyan
    Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" -OutFile $mavenZip
    Write-Host "Extracting Maven..." -ForegroundColor Cyan
    Expand-Archive -Path $mavenZip -DestinationPath $mavenDir
    Remove-Item -Path $mavenZip
    Write-Host "Maven set up completed successfully." -ForegroundColor Green
} else {
    Write-Host "Using existing local Maven..." -ForegroundColor Green
}

Write-Host "Setting MAVEN_OPTS..." -ForegroundColor Cyan
$env:MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"

Write-Host "Running project on Tomcat..." -ForegroundColor Cyan
& $mvnCmd clean tomcat7:run
