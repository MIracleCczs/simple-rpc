package pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author miracle
 * @version 1.0.0
 * @date 2021-04-15 15:12
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    
    private String username;

    private int age;

    private String sex;


}
