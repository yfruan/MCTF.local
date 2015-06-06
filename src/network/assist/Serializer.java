package network.assist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import network.address.Endpoint;
import network.address.NetworkInfo;
import network.protocol.Message;
import network.protocol.Payload;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Serializer {
	
	private static final boolean compress=false;
	
    private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() { 
    	@Override
    	protected Kryo initialValue() {
    		Kryo kryo = new Kryo();
    		kryo.setReferences(false);
    		kryo.setRegistrationRequired(true);
    		
    		//kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());  
    		//kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
    		
    		kryo.register(Message.class);
    		kryo.register(Payload.class);
    		kryo.register(Endpoint.class);
    		kryo.register(NetworkInfo.class);
    		kryo.register(NetworkInfo[].class);
    		kryo.register(ArrayList.class);
    		kryo.register(Collection.class);
    		kryo.register(HashSet.class);
    		kryo.register(HashMap.class);
            kryo.register(byte[].class);
            kryo.register(String[].class);
    		return kryo;
    	}
    };
    
    public static byte[] write(Object object){ 
        Kryo kryo = kryoThreadLocal.get(); 	
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Output output=new Output(byteArrayOutputStream);
        if(compress)
            output = new Output(new DeflaterOutputStream(byteArrayOutputStream));

        kryo.writeObject(output, object);
        output.flush();
        output.close();

        return byteArrayOutputStream.toByteArray();
     }

     public static Object read(byte[] data, Class<?> type){       	              	 
         Kryo kryo = kryoThreadLocal.get();        
         ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
         
         Input input=new Input(byteArrayInputStream);
         if(compress)
        	 input = new Input(new InflaterInputStream(byteArrayInputStream));

         return kryo.readObject(input,type);
     }
}
