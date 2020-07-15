const core = require('@actions/core');
const github = require('@actions/github');

async function run() {
	try {	  
	  const message = core.getInput('message');
	  const github_token = core.getInput('GITHUB_TOKEN');
	  
	  const context = github.context;
	  //console.log(context);
	  let pull_request_number = null;
	  
	  if (context.payload.pull_request != null){
		  pull_request_number = context.payload.pull_request.number;
	  } else if (context.payload.issue != null){
		  pull_request_number = context.payload.issue.number;
	  } else {
		  core.setFailed('No PR found!');
		  return;
	  }
	  	  
	  const octokit = new github.GitHub(github_token);
	  
	  const comment = octokit.issues.createComment({
		  ...context.repo,
		  issue_number: pull_request_number,
		  body: message
	  });  

	} catch (error) {
	  core.setFailed(error.message);
	}
}

run();
