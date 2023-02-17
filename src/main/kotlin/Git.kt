import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import java.nio.file.Path
import java.nio.file.Paths

class Git(repositoryURI: String) {

    private val git = cloneRepo(repositoryURI)

    val projectName: String = repositoryURI.substringAfterLast("/")

    private fun cloneRepo(repositoryURI: String) = Git.cloneRepository().setURI(repositoryURI).call()

    fun createCommitAndPushToGitHub(commitMessage: String) {
        try {
            val git = Git(git.repository)
            git.add().addFilepattern(".").call()
            git.commit().setMessage(commitMessage).call()
            val pushCommand =
                git.push().setCredentialsProvider(UsernamePasswordCredentialsProvider("username", "password"))
            pushCommand.call()
        } catch (e: GitAPIException) {
            println("Error occurred while creating commit and pushing to GitHub: ${e.message}")
        }
    }


    fun forkRepository(forkURI: String) = Git.cloneRepository().setURI(forkURI).setBranch("master").call()


    fun removeRepo() {
        git.repository.close()
        val path: Path = Paths.get(projectName)
        path.toFile().deleteRecursively()
    }

    fun getCommitRevisions() = git.log().all().call()


    fun getLastCommit() = git.repository.resolve(Constants.HEAD)


    fun readFileFromCommit(id: ObjectId, file: String): ByteArray? {
        val tree = RevWalk(git.repository).parseCommit(id).tree
        val treeWalk = TreeWalk(git.repository).apply {
            addTree(tree)
            isRecursive = true
            filter = PathFilter.create(file)
        }

        if (treeWalk.next()) {
            return git.repository.open(treeWalk.getObjectId(0)).bytes
        }
        return null
    }

    fun listTree(id: ObjectId) {
        val tree = RevWalk(git.repository).parseCommit(id).tree
        val treeWalk = TreeWalk(git.repository).apply {
            addTree(tree)
            isRecursive = true
        }

        while (treeWalk.next()) {
            println("found ${treeWalk.pathString}")
        }
    }
}