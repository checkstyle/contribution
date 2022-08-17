# Flow chart when user play need to do preparation for release

[Mermaid syntax](https://mermaid-js.github.io/mermaid/#/sequenceDiagram).

```mermaid
sequenceDiagram
    actor Maintainer
    participant Github

    Note left of Maintainer: version 10.4-SNAPSHOT
    Maintainer->>Github: run Github action "Bump version and Update Milestone '10.3.1'"
    par Github action
        Github->>Github: mvn version:set '10.3.1-SNAPSHOT'
        Github->>Github: push changes for version bump
        Github->>Github: update milestone name from 10.4 to 10.3.1
    end

    Note left of Maintainer: version 10.3.1-SNAPSHOT
    Maintainer->>Github: run Github action "Push releasenotes '10.3.1'"
    par Github action
        Github->>Github: run releasenotes-builder
        Github->>Github: update xdoc/releasenotes.xml
        Github->>Github: push code update
    end

    Maintainer->>Github: run Github action "Release prepare '10.3.1'"
    par Github action
        Github->>Github: mvn release:prepare
        Github->>Github: push code update and tag '10.3.1'
    end

    Maintainer->>Github: run Github action "Release perform '10.3.1'"
    par Github action
        Github->>Github: mvn release:perform
    end

    Maintainer->>Github: run Github action "Update Github '10.3.1'"
    par Github action
        Github->>Github: create/update Githubs milestone
        Github->>Github: create github release page and deploy '-all.jar'
    end

    Maintainer->>Github: run Github action "Copy site to sourceforge '10.3.1'"
    par Github action
        Github->>Github:copy site to sourceforge
    end

    Maintainer->>Github: run Github action "Copy site to sourceforge '10.3.1'"
    par Github action
        Github->>Github:run releasenotes-builder
        Github->>Github:tweet to public
        Github->>Github:update github release page with release notes
    end

    Note left of Maintainer: version 10.3.2-SNAPSHOT
    Maintainer->>Github: run Github action "set version '10.4-SNAPSHOT'"
    par Github action
        Github->>Github: mvn version:set '10.4-SNAPSHOT' from 10.3.2-SNAPSHOT
        Github->>Github: push changes for version bump
        Github->>Github: update milestone name from 10.3.2 to 10.4
    end
    Note left of Maintainer: version 10.4-SNAPSHOT
```
