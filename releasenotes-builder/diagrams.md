# Flow chart when user play need to do preparation for release

[Mermaid syntax](https://mermaid-js.github.io/mermaid/#/sequenceDiagram).

```mermaid
sequenceDiagram
    actor Maintainer
    participant Github
    participant CI
    
    Note left of Maintainer: version 10.4-SNAPSHOT
    Maintainer->>Github: run Github action "set version '10.3.1-SNAPSHOT'"
    par
        Github->>Github: mvn version:set '10.3.1-SNAPSHOT'
        Github->>Github: push changes for version bump
        Github->>Github: update milestone name from 10.4 to 10.3.1
    end
    
    Note left of Maintainer: version 10.3.1-SNAPSHOT
    Maintainer->>Github: create tag 'prepare-10.3.1'
    Github->>CI: trigger job
    CI->>CI: run releasenotes-builder
    CI->>CI: update xdoc/releasenotes.xml
    CI->>CI: mvn release:prepare
    CI->>Github: push code update and tag '10.3.1'

    Github->>CI: trigger by tag '10.3.1'
    CI->>CI: mvn release:perform
    CI->>CI: create/update Githubs milestone and deploy '-all.jar'
    
    Github->>CI: trigger by tag '10.3.1'
    CI->>CI: copy site to sourceforge

    Github->>CI: trigger by tag '10.3.1'
    CI->>CI: run releasenotes-builder
    CI->>CI: tweet to public
    CI->>CI: update githut release page with release notes

    Note left of Maintainer: version 10.3.2-SNAPSHOT
    Maintainer->>Github: run Github action "set version '10.4-SNAPSHOT'"
    par
        Github->>Github: mvn version:set '10.4-SNAPSHOT' from 10.3.2-SNAPSHOT
        Github->>Github: push changes for version bump
        Github->>Github: update milestone name from 10.3.2 to 10.4
    end
    Note left of Maintainer: version 10.4-SNAPSHOT
```
