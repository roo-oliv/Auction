package br.edu.ufabc.auction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class Converter {

    public static <T> T fromBytes(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in = new ObjectInputStream(bis);
        T t = (T)in.readObject();
        bis.close();
        in.close();
        return t;
    }

    public static <T> byte[] toBytes(T t) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(t);
        byte[] byteArray = bos.toByteArray();
        out.flush();
        bos.close();
        return byteArray;
    }
}
