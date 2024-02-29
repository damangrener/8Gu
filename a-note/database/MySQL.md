### CHAR 和 VARCHAR
+ char定长字符串
+ varchar变长字符串

#### char(M),varchar(M)的M
+ 代表能保存的字符数的最大值
+ 无论是字母数字还是中文，都只占用一个字符

### MySQL字段类型
+ 数值类型
  + 整形 
    + tinyint
    + smallint
    + mediumint
    + bigint
  + 浮点型
    + float
    + double
  + 定点型
    + decimal
+ 字符串类型
  + 常用
    + char
    + varchar
  + text
    + ...
  + blob
    + ...
+ 日期时间类型
  + date
  + datetime
  + timestamp
  + year
  + time

### datetime和timestamp的区别
+ datetime没有时区信息，timestamp和时区有关
+ datetime8字节存储，timestamp4字节
+ timestamp 1970-2037
+ datetime 1000-9999

### null和‘’的区别
+ ‘’的长度是0，不占用空间，NULL占用空间
+ 查询NULL必须用is null，is not null判断，不能用比较运算符
+ null影响聚合函数的结果

### 存储引擎
+ 5.5.5之前MyISAM，之后InnoDB
+ 存储引擎采用插件式架构，支持多种存储引擎
+ 存储引擎基于表不是基于数据库，所以可以为不同的表设置不同的存储引擎以适应不同场景的需要

#### myisam和innodb
+ myisam只有表级锁，innodb支持表级锁
+ myisam不提供事务支持
+ innodb支持事务，实现了四个隔离级别，默认可重复读
+ innodb支持mvcc，行级锁的升级，有效减少加锁操作，提高性能
+ 