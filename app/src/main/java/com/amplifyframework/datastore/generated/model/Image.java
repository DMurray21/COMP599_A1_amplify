package com.amplifyframework.datastore.generated.model;


import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Image type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Images")
public final class Image implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField USER = field("user");
  public static final QueryField TITLE = field("title");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String user;
  private final @ModelField(targetType="String", isRequired = true) String title;
  public String getId() {
      return id;
  }
  
  public String getUser() {
      return user;
  }
  
  public String getTitle() {
      return title;
  }
  
  private Image(String id, String user, String title) {
    this.id = id;
    this.user = user;
    this.title = title;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Image image = (Image) obj;
      return ObjectsCompat.equals(getId(), image.getId()) &&
              ObjectsCompat.equals(getUser(), image.getUser()) &&
              ObjectsCompat.equals(getTitle(), image.getTitle());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getUser())
      .append(getTitle())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Image {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("user=" + String.valueOf(getUser()) + ", ")
      .append("title=" + String.valueOf(getTitle()))
      .append("}")
      .toString();
  }
  
  public static UserStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   * @throws IllegalArgumentException Checks that ID is in the proper format
   */
  public static Image justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Image(
      id,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      user,
      title);
  }
  public interface UserStep {
    TitleStep user(String user);
  }
  

  public interface TitleStep {
    BuildStep title(String title);
  }
  

  public interface BuildStep {
    Image build();
    BuildStep id(String id) throws IllegalArgumentException;
  }
  

  public static class Builder implements UserStep, TitleStep, BuildStep {
    private String id;
    private String user;
    private String title;
    @Override
     public Image build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Image(
          id,
          user,
          title);
    }
    
    @Override
     public TitleStep user(String user) {
        Objects.requireNonNull(user);
        this.user = user;
        return this;
    }
    
    @Override
     public BuildStep title(String title) {
        Objects.requireNonNull(title);
        this.title = title;
        return this;
    }
    
    /** 
     * WARNING: Do not set ID when creating a new object. Leave this blank and one will be auto generated for you.
     * This should only be set when referring to an already existing object.
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     * @throws IllegalArgumentException Checks that ID is in the proper format
     */
    public BuildStep id(String id) throws IllegalArgumentException {
        this.id = id;
        
        try {
            UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
        } catch (Exception exception) {
          throw new IllegalArgumentException("Model IDs must be unique in the format of UUID.",
                    exception);
        }
        
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String user, String title) {
      super.id(id);
      super.user(user)
        .title(title);
    }
    
    @Override
     public CopyOfBuilder user(String user) {
      return (CopyOfBuilder) super.user(user);
    }
    
    @Override
     public CopyOfBuilder title(String title) {
      return (CopyOfBuilder) super.title(title);
    }
  }
  
}
