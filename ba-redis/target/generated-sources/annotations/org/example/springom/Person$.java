package org.example.springom;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.indexed.GeoField;
import com.redis.om.spring.metamodel.indexed.NumericField;
import com.redis.om.spring.metamodel.indexed.TagField;
import com.redis.om.spring.metamodel.indexed.TextField;
import com.redis.om.spring.metamodel.indexed.TextTagField;
import java.lang.Integer;
import java.lang.NoSuchFieldException;
import java.lang.SecurityException;
import java.lang.String;
import java.lang.reflect.Field;
import java.util.Set;
import org.springframework.data.geo.Point;

public final class Person$ {
  public static Field firstName;

  public static Field age;

  public static Field skills;

  public static Field personalStatement;

  public static Field id;

  public static Field address;

  public static Field lastName;

  public static Field homeLoc;

  public static String ADDRESS_COUNTRY;

  public static String ADDRESS_STATE;

  public static String ADDRESS_CITY;

  public static String ADDRESS_STREET;

  public static String ADDRESS_HOUSE_NUMBER;

  public static String ADDRESS_POSTAL_CODE;

  public static TextTagField<Person, String> FIRST_NAME;

  public static NumericField<Person, Integer> AGE;

  public static TagField<Person, Set<String>> SKILLS;

  public static TextField<Person, String> PERSONAL_STATEMENT;

  public static TextTagField<Person, String> ID;

  public static MetamodelField<Person, Address> ADDRESS;

  public static TextTagField<Person, String> LAST_NAME;

  public static GeoField<Person, Point> HOME_LOC;

  static {
    try {
      firstName = Person.class.getDeclaredField("firstName");
      age = Person.class.getDeclaredField("age");
      skills = Person.class.getDeclaredField("skills");
      personalStatement = Person.class.getDeclaredField("personalStatement");
      id = Person.class.getDeclaredField("id");
      address = Person.class.getDeclaredField("address");
      lastName = Person.class.getDeclaredField("lastName");
      homeLoc = Person.class.getDeclaredField("homeLoc");
      FIRST_NAME = new TextTagField<Person, String>(firstName,true);
      AGE = new NumericField<Person, Integer>(age,true);
      SKILLS = new TagField<Person, Set<String>>(skills,true);
      PERSONAL_STATEMENT = new TextField<Person, String>(personalStatement,true);
      ID = new TextTagField<Person, String>(id,true);
      ADDRESS = new MetamodelField<Person, Address>(address,true);
      LAST_NAME = new TextTagField<Person, String>(lastName,true);
      HOME_LOC = new GeoField<Person, Point>(homeLoc,true);
      ADDRESS_COUNTRY = new String("address_country");
      ADDRESS_STATE = new String("address_state");
      ADDRESS_CITY = new String("address_city");
      ADDRESS_STREET = new String("address_street");
      ADDRESS_HOUSE_NUMBER = new String("address_houseNumber");
      ADDRESS_POSTAL_CODE = new String("address_postalCode");
    } catch(NoSuchFieldException | SecurityException e) {
      System.err.println(e.getMessage());
    }
  }
}
