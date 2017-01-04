import org.eclipse.jgit.api.errors.NoFilepatternException
import org.eclipse.jgit.internal.storage.file.FileRepository
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
        String lauraRemotePath = "https://github.com/ticketbis/laura.git"
        String lauraNewBranch = "release/201701032002"
        String lauraOldBranch = "release/201701022004"

        String hulkLocalPath = "c:\\TB\\ticketbis\\hulk\\"
        String hulkRemotePath = "https://github.com/ticketbis/hulk.git"

        String colossusLocalPath = "c:\\TB\\ticketbis\\colossus\\"
        String colossusRemotePath = "https://github.com/ticketbis/colossus.git"

        GitControl lauraGC = new GitControl(lauraLocalPath, lauraRemotePath)
        if (!new File(lauraLocalPath).exists()) lauraGC.cloneRepo()
        lauraGC.checkoutBranch(lauraNewBranch)
        lauraGC.printLog()






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
                .call()
    }

    void pullFromRepo() throws IOException, WrongRepositoryStateException,
            InvalidConfigurationException, DetachedHeadException,
            InvalidRemoteException, CanceledException, RefNotFoundException,
            NoHeadException, GitAPIException {
        this.git.pull().call();
    }

    void checkoutBranch(String branch){
        this.git.checkout()
                .setName(branch)
                .call()
    }

    void printLog(){
        Iterable<RevCommit> commitList = this.git.log().call()
        commitList.each {println(it)}
    }
}


