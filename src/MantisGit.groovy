import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.errors.NoFilepatternException
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.ObjectReader
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
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.patch.HunkHeader
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevTree
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser

class MantisGit {
    void test() {
        String lauraLocalPath = "c:\\TB\\ticketbis\\laura\\"
        //String lauraLocalPath = "F:\\PortableGit\\ticketbis\\laura\\"
        String lauraRemotePath = "https://github.com/ticketbis/laura.git"
        String lauraNewBranch = "release/201701102001"
        String lauraOldBranch = "release/201701092003"

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
        //lauraGC.printLogBetweenBranches(lauraOldBranch, lauraNewBranch)
        //lauraGC.printDiffBetweenBranches(lauraOldBranch, lauraNewBranch)
        lauraGC.getCommitFromBranchName(lauraNewBranch)
        lauraGC.printDiffBetweenBranches(lauraOldBranch, lauraNewBranch)

        GitControl hulkGC = new GitControl(hulkLocalPath, hulkRemotePath)
        if (!new File(hulkLocalPath).exists()) hulkGC.cloneRepo()

        GitControl colossusGC = new GitControl(colossusLocalPath, colossusRemotePath)
        if (!new File(colossusLocalPath).exists()) colossusGC.cloneRepo()


    }


}

class GitControl {
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

    void refresh() {
        this.git.fetch()
                .setCredentialsProvider(this.credentialsProvider)
                .setCheckFetchedObjects(true)
                .call()
        this.git.pull()
                .setCredentialsProvider(this.credentialsProvider)
                .call()
    }

    void checkoutBranch(String branch) {
        boolean branchNotExists = this.localRepo.resolve(branch) == null;
        if (branchNotExists) {
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

    void printLogBetweenBranches(final String revFrom, final String revTo) throws IOException, GitAPIException {
        ObjectId refFrom = this.localRepo.resolve(revFrom)
        ObjectId refTo = this.localRepo.resolve(revTo)
        Iterable<RevCommit> commitList = this.git.log().addRange(refFrom, refTo).call()
        int commitNumber = 0
        commitList.each {
            if (it.authorIdent.name != 'jenkins') {
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

    void printDiffBetweenBranches(String oldBranchName, String newBranchName) {
        ObjectReader reader = this.localRepo.newObjectReader()
        Ref oldRef = this.localRepo.exactRef('refs/heads/' + oldBranchName)
        Ref newRef = this.localRepo.exactRef('refs/heads/' + newBranchName)

        AbstractTreeIterator oldTreeIterator = new CanonicalTreeParser()
        AbstractTreeIterator newTreeIterator = new CanonicalTreeParser()

        RevTree oldTree = new RevWalk(this.localRepo).parseTree(oldRef.getObjectId())
        RevTree newTree = new RevWalk(this.localRepo).parseTree(newRef.getObjectId())

        oldTreeIterator.reset(reader, oldTree.getId())
        newTreeIterator.reset(reader, newTree.getId())

        def diffs = this.git.diff()
                .setNewTree(newTreeIterator)
                .setOldTree(oldTreeIterator)
                .call()

        int numberOfFilesChanged = diffs.size()
        println('   :: FICHEROS MODIFICADOS: ' + numberOfFilesChanged)

        for (DiffEntry entry : diffs) {
            DiffFormatter formatter = new DiffFormatter(System.out)
            formatter.setRepository(this.localRepo)
            formatter.format(entry)
            FileHeader fileHeader = formatter.toFileHeader( entry );
            List<? extends HunkHeader> hunks = fileHeader.getHunks();
            for( HunkHeader hunk : hunks ) {
                println('   :: LINES_CONTEXT: ' + hunk.linesContext)
                println('   :: BUFFER: ' + hunk.buffer)
                println('   :: START_OFFSET: ' + hunk.startOffset)
                println('   :: END_OFFSET: ' + hunk.endOffset)
                println('   :: FILE_HEADER: ' + hunk.fileHeader)
                println('   :: NEW_LINE_COUNT: ' + hunk.newLineCount)
                println('   :: NEW_START_LINE: ' + hunk.newStartLine)
                println()
                //println( hunk );
            }

        }
    }

    RevCommit getCommitFromBranchName(String branchName) throws IOException {
        Ref thisBranch = this.localRepo.exactRef("refs/heads/" + branchName);
        println("Found branch: " + branchName);

        RevWalk walk = new RevWalk(this.localRepo)
        RevCommit commit = walk.parseCommit(thisBranch.getObjectId());
        //ObjectId id = this.localRepo.resolve("38d51408bd");
        //RevCommit commitAgain = walk.parseCommit(id);
        println("Found Commit again: " + commit.name);
        return commit

        walk.dispose();
    }


}


