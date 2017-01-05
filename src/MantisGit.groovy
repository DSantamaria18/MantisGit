import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.errors.NoFilepatternException
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.CanceledException
import org.eclipse.jgit.api.errors.DetachedHeadException
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.InvalidConfigurationException
import org.eclipse.jgit.api.errors.InvalidRemoteException
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.api.errors.RefNotFoundException
import org.eclipse.jgit.api.errors.WrongRepositoryStateException
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

class MantisGit {
    void test() {
        String lauraLocalPath = "c:\\TB\\ticketbis\\laura\\"
        //String lauraLocalPath = "F:\\PortableGit\\ticketbis\\laura\\"
        String lauraRemotePath = "https://github.com/ticketbis/laura.git"
        String lauraNewBranch = "release/201701042002"
        String lauraOldBranch = "release/201701032002"

        String hulkLocalPath = "c:\\TB\\ticketbis\\hulk\\"
        //String hulkLocalPath = "F:\\PortableGit\\ticketbis\\hulk\\"
        String hulkRemotePath = "https://github.com/ticketbis/hulk.git"

        String colossusLocalPath = "c:\\TB\\ticketbis\\colossus\\"
        //String colossusLocalPath = "F:\\PortableGit\\ticketbis\\colossus\\"
        String colossusRemotePath = "https://github.com/ticketbis/colossus.git"

        GitControl lauraGC = new GitControl(lauraLocalPath, lauraRemotePath)
        (!new File(lauraLocalPath).exists()) ? lauraGC.cloneRepo() : lauraGC.refresh()
        lauraGC.checkoutBranch(lauraOldBranch)
        lauraGC.checkoutBranch(lauraNewBranch)
        lauraGC.printLogBetween(lauraOldBranch, lauraNewBranch)

        GitControl hulkGC = new GitControl(hulkLocalPath, hulkRemotePath)
        if (!new File(hulkLocalPath).exists()) hulkGC.cloneRepo()

        GitControl colossusGC = new GitControl(colossusLocalPath, colossusRemotePath)
        if (!new File(colossusLocalPath).exists()) colossusGC.cloneRepo()




    }


}

class GitControl{
    private String localPath, remotePath
    private Repository localRepo
    private Git git
    private CredentialsProvider credentialsProvider
    private String username = "DSantamaria18"
    private String password = "45671002v"

    GitControl(String localPath, String remotePath) {
        this.localPath = localPath
        this.remotePath = remotePath
        this.localRepo = new FileRepository(localPath + "\\.git")
        this.credentialsProvider = new UsernamePasswordCredentialsProvider(this.username, this.password)
        this.git = new Git(localRepo)
    }

    void cloneRepo() throws IOException, NoFilepatternException, GitAPIException {
        this.git.cloneRepository()
                .setURI(this.remotePath)
                .setDirectory(new File(this.localPath))
                .setCredentialsProvider(this.credentialsProvider)
                .setCloneAllBranches(true)
                .call()
    }

    void refresh(){
        this.git.fetch()
                .setCredentialsProvider(this.credentialsProvider)
                .setCheckFetchedObjects(true)
                .call()
        this.git.pull()
                .setCredentialsProvider(this.credentialsProvider)
                .call()
    }

    void checkoutBranch(String branch){
        boolean branchNotExists = this.localRepo.resolve(branch) == null;
        if(branchNotExists){
            this.git.branchCreate()
                    .setName(branch)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                    .setStartPoint('origin/' + branch)
                    .call()
        }
        this.git.checkout()
                .setName(branch)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                .call()
    }

    void printLogBetween(final String revFrom, final String revTo) throws IOException, GitAPIException {
        ObjectId refFrom = this.localRepo.resolve(revFrom);
        ObjectId refTo = this.localRepo.resolve(revTo);
        Iterable<RevCommit> commitList = this.git.log().addRange(refFrom, refTo).call();
        int commitNumber = 0
        commitList.each {
            if(it.authorIdent.name != 'jenkins'){
                println('=================================================================')
                println('HASH:' + it.name)
                println('AUTHOR: ' + it.authorIdent.name)
                println('EMAIL: ' + it.authorIdent.emailAddress)
                println('DATE: ' + it.authorIdent.when)
                println('MESSAGE: ' + it.fullMessage)
                commitNumber++
            }
        }
        println()
        println('TOTAL COMMITS: ' + commitNumber)
    }
}


