import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.dao.utils.BeanContext;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.zt.cod.ZtCodTask;
import org.apache.dolphinscheduler.zt.service.service.impl.TaskLogServiceImpl;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * @Author WTF
 * @Date 2023/10/23 16:19:16
 * @Desc
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class FolderTest {


    @Mock
    private TaskExecutionContext taskExecutionContext;

    private ApplicationContext applicationContext;

    @Before
    public void setUp() {
        // 在每个测试方法执行前初始化TaskExecutionContext对象
        // 这里可以使用任何适合的方法来创建Mock对象或者实际对象
        taskExecutionContext = new TaskExecutionContext(/* 传递所需参数 */);
    }

    @Test
    public void testZtCodTaskInstantiation() {
        // 实例化 ZtCodTask 类
        ZtCodTask ztCodTask = new ZtCodTask(taskExecutionContext);

        // 在这里可以添加断言来验证实例化过程是否如预期
        // 例如，您可以验证类的属性是否被正确初始化
        // 例如，使用断言来检查 taskExecutionContext 是否与传递的对象相同
//        assertEquals(taskExecutionContext, ztCodTask.getTaskExecutionContext());
    }
}
