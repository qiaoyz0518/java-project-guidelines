package cn.tendata.jstart.data.domain;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;

/**
 * Interface for auditable entities. Allows storing and retrieving creation and modification
 * information. The changing instance (typically some user) is to be defined by a generics
 * definition.
 *
 * @param <ID> the type of the audited type's identifier
 * @author Xu Cheng
 */
@MappedSuperclass
public abstract class AbstractEntityAuditable<ID extends Serializable> extends AbstractEntity<ID> {

	private static final long serialVersionUID = 7479587631743834284L;

	private Date createdDate;
	private Date lastModifiedDate;

	/**
	 * Returns the creation date of the entity.
	 *
	 * @return the createdDate
	 */
	@CreatedDate
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * Sets the creation date of the entity.
	 *
	 * @param createdDate the creation date to set
	 */
	public void setCreatedDate(final Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * Returns the date of the last modification.
	 *
	 * @return the lastModifiedDate
	 */
	@LastModifiedDate
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	/**
	 * Sets the date of the last modification.
	 *
	 * @param lastModifiedDate the date of the last modification to set
	 */
	public void setLastModifiedDate(final Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
}
