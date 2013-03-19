package com.zenred.mongo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.MongoException;

import com.zenred.Repository;
import com.zenred.RepositoryException;

public class BaseRepositoryMorphiaImpl <AnObject> implements Repository<String, AnObject> {

	/**
	 * retry count and number of sleeping seconds.  Should be changed so this is externally configurable
	 */
	private int RETRY_LIMIT = 5;
	private int N_SECONDS = 4000;
	
	private Datastore datastore;
	
	private Class<AnObject> type;
	
	/**
	 * wrapper for getEntityClass
	 * 
	 * @return Class instance of <AnObject>
	 * 
	 */
	protected Class<AnObject> getClassType() {
		if(type == null)
			type = getEntityClass();
		return type;
	}
	
	/**
	 * Constructor with delegated Datastore
	 * @param datastore
	 */
	public BaseRepositoryMorphiaImpl(Datastore datastore) {
		this.datastore = datastore;
	}
	
	/**
	 * 
	 * @return actual Class of <AnObject> using reflection API
	 */
	private Class<AnObject> getEntityClass() {
		Type type = getClass().getGenericSuperclass();
		ParameterizedType paramType = (ParameterizedType)type;
		@SuppressWarnings("unchecked")
		Class<AnObject> entityClass = (Class<AnObject>) paramType.getActualTypeArguments()[0];
		return entityClass;
	}
	/**
	 * wrapper for Datastore instance
	 * 
	 * @return Datastore
	 */
	protected Datastore getDatastore() {
		return datastore;
	}
	/**
	 * If there is a write failure, give the mongo replica set time to recover
	 * 
	 * @param <AnObject> Object to  be persisted
	 * @exception RepositoryException
	 */
	@Override
	public void create(AnObject obj) throws RepositoryException {
		int retry = 0;
		try {
			datastore.save(obj);
		} catch (MongoException me) {
			me.printStackTrace();
			for (retry = 0; retry < RETRY_LIMIT;) {
				try {
					Thread.sleep(N_SECONDS);
				} catch (InterruptedException ie) {
					throw new RepositoryException("Thread failure ! ", ie);
				}
				try {
					datastore.save(obj);
				} catch (MongoException me_nested) {
					me_nested.printStackTrace();
					++retry;
				}
			}

		} finally {
			if (RETRY_LIMIT == retry) {
				throw new RepositoryException("write for " + obj
						+ " has failed after " + RETRY_LIMIT + " retries");
			}
		}
	}

	/**
	 * @param unique identifier in Mongo database
	 * @return <AnObject> Object
	 * @exception RepositoryException
	 */
	@Override
	public AnObject read(String id) throws RepositoryException {
		return datastore.get(getClassType(), id);
	}
	/**
	 * @param <AnObject> Object instance updated
	 * @exception RepositoryException
	 */
	@Override
	public void update(AnObject obj) throws RepositoryException {
		datastore.merge(obj);
	}
	/**
	 * @param <AnObject> Object instance removed from the Mongo database
	 * @exception RepositoryException
	 */
	@Override
	public void delete(AnObject obj) throws RepositoryException {
		datastore.delete(obj);
	}
	/**
	 * @return List of <AnObject> Objects
	 * @exception RepositoryException
	 */
	@Override
	public List<AnObject> findAll() throws RepositoryException {
		return datastore.find(getClassType()).asList();
	}

	/**
	 * 
	 * @param object
	 * @param properties
	 * @throws RepositoryException
	 */
	// Implementation  specific methods TODO: move me
	public void update(AnObject object, Map<String, Object> properties) throws RepositoryException {
		UpdateOperations<AnObject> ops = datastore.createUpdateOperations(getClassType());
		for(String key : properties.keySet())
			ops.set(key, properties.get(key));
		datastore.update(object, ops);		
	}
	/**
	 * 
	 * @param property
	 * @param value
	 * @return List of <AnObject> Objects
	 * @throws RepositoryException
	 */
	// Implementation  specific methods  TODO: move me
	public List<AnObject> findByPropertyValue(String property, Object value)
			throws RepositoryException {
		Query<AnObject> query = datastore.createQuery(getClassType()).field(property).equal(value);
		query.disableValidation();
		return query.asList();
	}
	/**
	 * 
	 * @param keyValueMap
	 * @return List of <AnObject> Objects
	 * @throws RepositoryException
	 */
	// Implementation  specific methods  TODO: move me
	public List<AnObject> findByPropertyValueMap(Map<String, Object> keyValueMap)
			throws RepositoryException {
		Query<AnObject> query = datastore.createQuery(getClassType());
		for(String key : keyValueMap.keySet()) {
			Object value = keyValueMap.get(key);
			query.field(key).equal(value);
		}
		query.disableValidation();
		return query.asList();
	}
	

	
	// Implemtation specific methods  TODO: move me
	public void deleteAll() throws RepositoryException {
		Query<AnObject> query = datastore.createQuery(getClassType());
		datastore.delete(query);
	}
	
	public List<AnObject> findByPropertyValues(String property, List<Object> values) {
		Query<AnObject> query = datastore.createQuery(getClassType()).field(property).hasAnyOf(values);
		return query.asList();
	}
	
	public List<AnObject> findAll(String sortProperty, Boolean ascending) {
		if(!ascending)
			sortProperty = "-".concat(sortProperty);
		Query<AnObject> query = datastore.createQuery(getClassType()).order(sortProperty);
		return query.asList();
	}

	
	public List<AnObject> findAllByKey(String keyName, String id, String sortProperty, Boolean ascending, int limit) throws RepositoryException {
		if(!ascending)
			sortProperty = "-".concat(sortProperty);
		Query<AnObject> query = datastore.createQuery(getClassType()).field(keyName).equal(id).order(sortProperty).limit(limit);
		return query.asList();
	}
	
	public List<AnObject> findAllByKeys(String keyName, String keyName2, String id, String id2, String sortProperty, Boolean ascending, int limit) throws RepositoryException {
		if(!ascending)
			sortProperty = "-".concat(sortProperty);
		Query<AnObject> query = datastore.createQuery(getClassType()).field(keyName).equal(id).field(keyName2).equal(id2).order(sortProperty).limit(limit);
		return query.asList();
	}
	
	
	
}

