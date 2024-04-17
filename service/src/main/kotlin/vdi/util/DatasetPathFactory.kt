package vdi.util

import org.veupathdb.vdi.lib.common.field.DatasetID
import org.veupathdb.vdi.lib.common.field.ProjectID
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists

class DatasetPathFactory(private val rootDirectory: String, private val siteBuild: String) {

  private val rootPath = Path.of(rootDirectory, siteBuild)

  init {
    // Ensure the mount point exists, or throw an exception.  The mount point
    // should be created by the systems team when they set up the service stack.
    if (rootPath.notExists())
      throw IllegalStateException("configured user dataset directory root path '$rootPath' does not exist.")
  }

  // {mount-path}/{build}/{project}/{dataset-id}
  fun makePath(project: ProjectID, datasetID: DatasetID): Path {
    val siteDir = rootPath.resolve(project)

    // If this is the first time we're seeing a path for a given target project,
    // create the project directory.
    if (siteDir.notExists())
      siteDir.createDirectory()

    return siteDir.resolve(datasetID.toString())
  }
}