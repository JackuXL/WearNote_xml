package cn.wearbbs.note.util

import android.os.Environment
import cn.wearbbs.note.database.bean.Note
import java.io.File

class DealFile {
    companion object {
         val externalStoragePath = Environment.getExternalStorageDirectory().path
         val appDataDirectoryPath = "$externalStoragePath/Android/data/cn.wearbbs.note/"
        fun convertTxtToNoteList(txtFiles: List<File>): List<Note> {
            val noteList = mutableListOf<Note>()
            for (file in txtFiles) {
                val name = file.nameWithoutExtension
                val content = file.readText()
                val createTime = file.lastModified()
                val note = Note(name = name, content = content, createTime = createTime)
                noteList.add(note)
            }
            return noteList
        }

        fun clearFolder(directoryPath: String) {
            val folder = File(directoryPath)
            if (folder.exists() && folder.isDirectory) {
                val files = folder.listFiles()
                if (files != null) {
                    for (file in files) {
                        if (file.isDirectory) {
                            clearFolder(file.path) // 递归清空子文件夹
                        } else {
                            file.delete() // 删除文件
                        }
                    }
                }
            }
        }

        fun filterTxtFiles(directoryPath: String): List<File> {
            val directory = File(directoryPath)
            return directory.listFiles { file ->
                file.isFile && file.extension.equals("txt", ignoreCase = true)
            }?.toList() ?: emptyList()
        }
    }


}