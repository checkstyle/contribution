# Flow chart when user play need to do preparation for release

[Mermaid syntax](https://mermaid-js.github.io/mermaid/#/sequenceDiagram).

```mermaid
sequenceDiagram
    actor Maintainer
    participant Github
    participant CI
    Note left of Maintainer: version 10.4-SNAPSHOT
    par if version bump is not as sequential
        Maintainer->>Maintainer: mvn version:set '10.3.1-SNAPSHOT'
        Maintainer->>Github: push changes for version bump
        Maintainer->>Github: update milestone name from 10.4 to 10.3.1
    end
    Note left of Maintainer: version 10.3.1-SNAPSHOT
    Maintainer->>Github: create tag 'prepare-10.3.1'
    Github->>CI: trigger job
    CI->>CI: update xdoc/releasenotes.xml
    CI->>CI: mvn release:prepare
    CI->>Github: push code update and tag '10.3.1'

    Github->>CI: trigger by tag '10.3.1'
    CI->>CI: mvn release:perform
    CI->>CI: create/update Githubs milestone and deploy '-all.jar'
    CI->>CI: copy site to sourceforge

    Github->>CI: trigger by tag '10.3.1'
    CI->>CI: tweet to public (before release page update)
    CI->>CI: update githut release page with release notes

    Note left of Maintainer: version 10.3.2-SNAPSHOT
    par if version bump is not as sequential
        Maintainer->>Maintainer: mvn version:set '10.4-SNAPSHOT' from 10.3.2-SNAPSHOT
        Maintainer->>Github: push changes for version bump
        Maintainer->>Github: update milestone name from 10.3.2 to 10.4
    end
    Note left of Maintainer: version 10.4-SNAPSHOT
```
