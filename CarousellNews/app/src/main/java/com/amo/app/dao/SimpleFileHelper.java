package com.amo.app.dao;

import android.annotation.TargetApi;
import android.os.Build;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class SimpleFileHelper {
    private static FileHelper INSTANCE = new FileHelper();

    public static FileHelper getInstance() {
        return INSTANCE;
    }

    private byte[] readBytesFromFile(String path) {
        Logger.d(">>> path:" + path);
        FileInputStream fis;
        File file;
        long file_length;
        byte[] buffer;
        int offset = 0;
        int next;
        file = new File(path);
        if (!file.exists()) {
            Logger.e("!!! file doesn't exists");
            return null;
        }
        if (file.length() > Integer.MAX_VALUE) {
            Logger.e("!!! file length is out of max of int");
            return null;
        } else {
            file_length = file.length();
        }
        try {
            fis = new FileInputStream(file);
            buffer = new byte[(int) file_length];
            while (true) {
                next = fis.read(buffer, offset, (buffer.length-offset));
                if (next < 0 || offset > buffer.length) break;
                offset += next;
            }
            if (offset < buffer.length) {
                Logger.e("!!! not complete to read");
                return null;
            }
            fis.close();
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e("!!! IOException");
            return null;
        }
    }

    public byte[] readBytesFromFile(String path, boolean is_simple) {
        if (is_simple) {
            return readBytesFromFile(path);
        }
        Logger.d(">>> path:" + path);
        FileInputStream fis;
        File file;
        BufferedInputStream bis;
        ByteArrayOutputStream bos;
        byte[] buf = new byte[1024];
        int num_read;
        file = new File(path);
        if (!file.exists()) {
            Logger.e("!!! file doesn't exists");
            return null;
        }
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bos = new ByteArrayOutputStream();
            while (true) {
                num_read = bis.read(buf, 0, buf.length); //1024 bytes per call
                if (num_read < 0) break;
                bos.write(buf, 0, num_read);
            }
            buf = bos.toByteArray();
            fis.close();
            bis.close();
            bos.close();
            return buf;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logger.e("!!! FileNotFoundException");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e("!!! IOException");
            return null;
        }
    }

    public File writeBytesToFile(String dir, String name, byte[] data) {
        Logger.d(">>> dir:" + dir + ", name:" + name);
        FileOutputStream fos;
        BufferedOutputStream bos;
        File directory;
        File file;
        directory = new File(dir);
        if (!directory.exists()) {
            Logger.d("... make a dir");
            if (!directory.mkdirs()) {
                Logger.e("!!! failed to make a dir");
                return null;
            }
        }
        file = new File(name);
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(data, 0, data.length);
            bos.flush();
            bos.close();
            fos.close();
            if (file.exists()) {
                return file;
            } else {
                Logger.e("!!! file doesn't exists");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e("!!! IOException");
            return null;
        }
    }

    public void list(final String path, final String end, final List<File> files) {
        Logger.d(">>> path:" + path + ", end:" + end);
        File file = new File(path);
        if (file.isDirectory()) {
            for (File child : file.listFiles()){
                list(child.getAbsolutePath(), end, files);
            }
        } else if (file.isFile()) {
            if (end.equals("")) {
                files.add(file);
            } else {
                if (file.getName().endsWith(end)) files.add(file);
            }
        } else {
            Logger.e("!!! child is not file or directory");
        }
    }

    public boolean remove(final String path, final String end) {
        Logger.d(">>> path:" + path + ", end:" + end);
        File file = new File(path);
        boolean result = false;
        if (file.isDirectory()) {
            for (File child : file.listFiles()){
                result = remove(child.getAbsolutePath(), end);
            }
        } else if (file.isFile()) {
            if (end.equals("")) {
                result = file.delete();
            } else {
                if (file.getName().endsWith(end)) result = file.delete();
            }
        } else {
            Logger.e("!!! child is not file or directory");
        }
        return result;
    }


    @TargetApi(Build.VERSION_CODES.O)
    public byte[] readNIOBytesFromFile(String path) throws IOException {
        Logger.d(">>> path:" + path);
        if (!Files.exists(Paths.get(path), LinkOption.NOFOLLOW_LINKS)) {
            Logger.e("!!! file doesn't exists");
            return null;
        } else {
            return Files.readAllBytes(Paths.get(path));
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public File writeNIOBytesToFile(String dir, String name, byte[] data) {
        Logger.d(">>> dir:" + dir + ", name:" + name);
        Path path_dir;
        Path path_file;
        try {
            if (!Files.exists(Paths.get(dir), LinkOption.NOFOLLOW_LINKS)) {
                Logger.d("... make a dir");
                path_dir = Files.createDirectories(Paths.get(dir));
                if (path_dir == null) {
                    Logger.e("!!! failed to make a dir");
                    return null;
                }
            }
            path_file = Files.write(Paths.get(name), data);
            return path_file.toFile();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e("!!! IOException");
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void listNIO(final String dir, final String end, final List<File> files) throws IOException {
        Logger.d(">>> dir:" + dir + ", end:" + end);
        Files.walkFileTree(Paths.get(dir), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Logger.d("... file:" + dir.getFileName());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Logger.d("... file:" + file.getFileName());
                if (end.equals("")) {
                    files.add(file.toFile());
                } else {
                    if (file.endsWith(end)) files.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                Logger.d("... file:" + file.getFileName());
                if (end.equals("")) {
                    files.add(file.toFile());
                } else {
                    if (file.endsWith(end)) files.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Logger.d("... file:" + dir.getFileName());
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * recursion
     */
    private int factorial (int x) {
        if (x > 1) return (x*(factorial(x-1)));
        else if (x == 1) return x;
        else return 0;
    }
}
