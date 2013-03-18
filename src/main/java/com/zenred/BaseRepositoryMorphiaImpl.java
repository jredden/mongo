package com.zenred.mongo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.zenred.Repository;
import com.zenred.RepositoryException;
import com.mongodb.MongoException;

public class BaseRepositoryMorphiaImpl <T> implements Repository<String, T> {

	/**
	 * retry count and number of sleeping seconds.  Should be changed so this is externally configurable
	 */
	private int RETRY_LIMIT = 5;
	private int N_SECONDS = 4000;
	
	private Datastore datastore;
	
	private Class<T> type;
	
	/**
	 * wrapper for getEntityClass
	 * 
	 * @return Class instance of <T>
	 * 
	 */
	protected Class<T> getClassType() {
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
	 * @return actual Class of <T> using reflection API
	 */
	private Class<T> getEntityClass() {
		Type type = getClass().getGenericSuperclass();
		ParameterizedType paramType = (ParameterizedType)type;
		@SuppressWarnings("unchecked")
		Class<T> entityClass = (Class<T>) paramType.getActualTypeArguments()[0];
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
	 * @param <T> Object to  be persisted
	 * @exception RepositoryException
	 */
	@Override
	public void create(T obj) throws RepositoryException {
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
	 * @return <T> Object
	 * @exception RepositoryException
	 */
	@Override
	public T read(String id) throws RepositoryException {
		return datastore.get(getClassType(), id);
	}
	/**
	 * @param <T> Object instance updated
	 * @exception RepositoryException
	 */
	@Override
	public void update(T obj) throws RepositoryException {
		datastore.merge(obj);
	}
	/**
	 * @param <T> Object instance removed from the Mongo database
	 * @exception RepositoryException
	 */
	@Override
	public void delete(T obj) throws RepositoryException {
		datastore.delete(obj);
	}
	/**
	 * @return List of <T> Objects
	 * @exception RepositoryException
	 */
	@Override
	public List<T> findAll() throws RepositoryException {
		return datastore.find(getClassType()).asList();
	}

	/**
	 * 
	 * @param object
	 * @param properties
	 * @throws RepositoryException
	 */
	// Implementation  specific methods TODO: move me
	public void update(T object, Map<String, Object> properties) throws RepositoryException {
		UpdateOperations<T> ops = datastore.createUpdateOperations(getClassType());
		for(String key : properties.keySet())
			ops.set(key, properties.get(key));
		datastore.update(object, ops);		
	}
	/**
	 * 
	 * @param property
	 * @param value
	 * @return List of <T> Objects
	 * @throws RepositoryException
	 */
	// Implementation  specific methods  TODO: move me
	public List<T> findByPropertyValue(String property, Object value)
			throws RepositoryException {
		Query<T> query = datastore.createQuery(getClassType()).field(property).equal(value);
		query.disableValidation();
		return query.asList();
	}
	/**
	 * 
	 * @param keyValueMap
	 * @return List of <T> Objects
	 * @throws RepositoryException
	 */
	// Implementation  specific methods  TODO: move me
	public List<T> findByPropertyValueMap(Map<String, Object> keyValueMap)
			throws RepositoryException {
		Query<T> query = datastore.createQuery(getClassType());
		for(String key : keyValueMap.keySet()) {
			Object value = keyValueMap.get(key);
			query.field(key).equal(value);
		}
		query.disableValidation();
		return query.asList();
	}
	

	
	// Implemtation specific methods  TODO: move me
	public void deleteAll() throws RepositoryException {
		Query<T> query = datastore.createQuery(getClassType());
		datastore.delete(query);
	}
	
	public List<T> findByPropertyValues(String property, List<Object> values) {
		Query<T> query = datastore.createQuery(getClassType()).field(property).hasAnyOf(values);
		return query.asList();
	}
	
	public List<T> findAll(String sortProperty, Boolean ascending) {
		if(!ascending)
			sortProperty = "-".concat(sortProperty);
		Query<T> query = datastore.createQuery(getClassType()).order(sortProperty);
		return query.asList();
	}

	
	public List<T> findAllByKey(String keyName, String id, String sortProperty, Boolean ascending, int limit) throws RepositoryException {
		if(!ascending)
			sortProperty = "-".concat(sortProperty);
		Query<T> query = datastore.createQuery(getClassType()).field(keyName).equal(id).order(sortProperty).limit(limit);
		return query.asList();
	}
	
	public List<T> findAllByKeys(String keyName, String keyName2, String id, String id2, String sortProperty, Boolean ascending, int limit) throws RepositoryException {
		if(!ascending)
			sortProperty = "-".concat(sortProperty);
		Query<T> query = datastore.createQuery(getClassType()).field(keyName).equal(id).field(keyName2).equal(id2).order(sortProperty).limit(limit);
		return query.asList();
	}
	
	
	
}

