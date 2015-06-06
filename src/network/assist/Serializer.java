package network.assist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.DeflaterOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Serializer {
    private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() { 
    	@Override
    	protected Kryo initialValue() {
    		Kryo kryo = new Kryo();
    		kryo.setReferences(false);
    		//kryo.register();
    		return kryo;
    	}
    };
    
    public static byte[] write(Object object){ 
        //System.out.println("begin");
         //Kryo kryo = kryoThreadLocal.get();
         //System.out.println("end");
    	
        System.out.println("begin");
    	Kryo kryo = new Kryo();
        System.out.println("end");
    	
		kryo.setReferences(false);
         
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream);   
         
         Output output = new Output(deflaterOutputStream);

         //kryo.writeClassAndObject(output, object);

         kryo.writeObject(output, object);
         output.flush();
         output.close();

         return byteArrayOutputStream.toByteArray();
     }

     public static Object read(byte[] data, Class<?> type){         
         ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
         Input input = new Input(byteArrayInputStream);
         Kryo kryo = kryoThreadLocal.get();
         return kryo.readObject(input,type);
     }
}
