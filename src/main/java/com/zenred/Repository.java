package com.zenred;

import java.io.Serializable;
import java.util.List;

import com.zenred.RepositoryException;

/**
 * Repository interface for type-safe CRUD operations
 * @param <PrimaryKey>
 * @param <AnObject>
 * 
 * @category interface
 * 
 */
public interface Repository <PrimaryKey extends Serializable, AnObject> {

	public void create(AnObject obj) throws RepositoryException;
	
	public AnObject read(PrimaryKey id) throws RepositoryException;
	
	public void update(AnObject obj) throws RepositoryException;
	
	public void delete(AnObject obj) throws RepositoryException;
	
	public List<AnObject> findAll() throws RepositoryException;
	
	
	
}
