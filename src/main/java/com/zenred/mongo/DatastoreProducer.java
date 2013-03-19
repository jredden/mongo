package com.zenred.mongo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.zenred.RepositoryException;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

/**
 * Constructs a mongo db datastore
 * 
 *
 */
public class DatastoreProducer {

	private Logger log = LoggerFactory.getLogger(DatastoreProducer.class);

	/**
	 * Retrievs Datastore
	 * 
	 * It is now using a Replica Set defined in the properties file as a comma delimited set of URIs
	 * 
	 * @return morphia Datastore
	 */
	public Datastore produceDataStore() throws RepositoryException {
		Datastore datastore = null;

		String propsFileName = this.getClass().getSimpleName() + ".properties";
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(propsFileName);
		if (is != null) {
			try {
				Properties props = new Properties();
				props.load(is);
				String dbName = props.getProperty("dbName");
				String dbHost = props.getProperty("dbHost");
				String mapPackage = props.getProperty("mapPackage");

				List addressList = new ArrayList<ServerAddress>();
				String [] mongoUri = dbHost.split(",");
				for (String aMongoUri : mongoUri){
					String URLandDbAddress [] = aMongoUri.split(":");
					addressList.add(new ServerAddress(URLandDbAddress[0], Integer.parseInt(URLandDbAddress[1])));
				}
				log.info("Connecting to database {} on server {}", dbName, addressList);

				Mongo m = new Mongo(addressList);
				Morphia morphia = new Morphia();
				morphia.mapPackage(mapPackage);
				datastore = morphia.createDatastore(m, dbName);
				datastore.ensureIndexes();

			} catch (Exception e) {
				log.info("Unable to connect to database", e);
			}

		}
		return datastore;
	}

}
