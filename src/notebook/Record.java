
package notebook;
import java.io.*;

// класс Record, представляющий отдельную запись
// применяет имнтерфейс Serializable для поддержки сериализации
public class Record implements Serializable 
{
    private String name; 
    private String birthday; 
    private String email; 
    private String group; 

    //метод возвращающий имя пользователя
    public String getName() {
        return name;
    }

    /**
     * метод, устанавливающий имя пользователя
     * @param name - устнавливаемое имя
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String dob) {
        this.birthday = dob;
    }

    // метод, возвращающий электронную почту пользователя
    public String getEmail() {
        return email;
    }

    // метод, устанавливающий элеметронную почту пользователя
    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }
    // метод проверки эквивалентности записей
    public boolean equals(Record o)
    {
        // ели имя и фамилия совпадают. значит записи идентичны
        if(this.getName().equals(o.getName()) && this.getBirthday().equals(o.getBirthday()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
