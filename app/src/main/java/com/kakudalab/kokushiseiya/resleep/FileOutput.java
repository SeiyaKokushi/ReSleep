package com.kakudalab.kokushiseiya.resleep;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * ファイルへの書き込みを行うクラス
 */
public class FileOutput {
    /**
     * ファイルへ文字列を書き込み
     * @param context
     * @param str ファイル出力文字列
     * @param fileName ファイル名
     */
    public static void writeFile(Context context, String str, String fileName) {
        writeBinaryFile(context, str.getBytes(), fileName);
        writeBinaryFile(context, "\n".getBytes(), fileName);
    }

    /**
     * ファイルへバイナリデータを書き込み
     * @param context
     * @param data バイトデータ
     * @param fileName ファイル名
     */
    public static void writeBinaryFile(Context context, byte[] data, String fileName) {
        OutputStream out = null;
        try {
            out = context.openFileOutput(fileName, Context.MODE_PRIVATE|Context.MODE_APPEND);
            out.write(data, 0, data.length);
        } catch (Exception e) {
            // 必要に応じて
//            throw e;
        } finally {
            try {
                if (out != null) out.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * ファイルから文字列を読み込む
     * @param context
     * @param fileName ファイル名
     * @return 文字列 ファイルがない場合はnull
     */
    public static String readFile(Context context, String fileName) {
        String str = null;
        byte[] data = readBinaryFile(context, fileName);
        if (data != null) {
            str = new String(data);
        }
        return str;
    }

    /**
     * ファイルからバイナリデータを読み込む
     * @param context
     * @param fileName
     * @return バイトデータ ファイルがない場合はnull
     */
    public static byte[] readBinaryFile(Context context, String fileName) {
        // ファイルの存在チェック
        if (!(new File(context.getFilesDir().getPath() + "/" + fileName).exists())) {
            return null;
        }

        int size;
        byte[] data = new byte[1024];
        InputStream in = null;
        ByteArrayOutputStream out = null;

        try {
            in = context.openFileInput(fileName);
            out = new ByteArrayOutputStream();
            while ((size = in.read(data)) != -1) {
                out.write(data, 0, size);
            }
            return out.toByteArray();
        } catch (Exception e) {
            // エラーの場合もnullを返すのでここでは何もしない
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (Exception e) {
            }
        }

        return null;
    }
}
