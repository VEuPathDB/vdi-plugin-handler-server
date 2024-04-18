package vdi.util

import org.veupathdb.vdi.lib.common.field.DatasetID
import java.nio.file.Path

class DatasetPathFactory(rootDirectory: String, siteBuild: String) {

  // {mount-path}/{build}/{project}/{dataset-id}
  fun makePath(project: String, datasetID: DatasetID): Path {
    return Path.of(rootDirectory, siteBuild, project, datasetID.toString())
  }
}