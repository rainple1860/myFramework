/**
 * @Auther: Administrator
 * @Date: 2018/9/25 09:43
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.aop.MethodHandler;
import com.rainple.framework.utils.ClassUtils;
import org.junit.Test;

import java.util.List;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-25 09:43
 **/
public class JunitTest {

    @Test
    public void testGetSubclass(){
        List<Class> childFromSuper = ClassUtils.getChildFromSuperToInstance(MethodHandler.class);
        System.out.println(childFromSuper);
    }


}
