To create and work with tables put @GenerateTable to class and @TableField annotations to methods  
List of awaible commands can be shown by /help command  

@GenerateTable must be used with title attribute to set table name    

@TableField can be used with attributes:  

referenceClassName to set referenced class (foreign key is automatically selected as referent class primary key)  
(Table can have 1 maximum refrence)  

isPrimaryKey to set field as priamary key  

isUnique to set field as unique  
