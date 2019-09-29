# hashMap理解

## 1.环境

jdk：1.8

## 1.1 介绍

本文介绍将讨论开发中最流行java集合框架中实现`map`接口`HashMap`,讨论之前，需要重点说明下`List`和`Set`都继承`Collection`但`Map`没有。

`HashMap` 中`put`方法增加元素，也提供其他api如增加、获取和修改数据不同方式，`hashTable`基于这些接口实现，看上去很复杂其实很容易理解。

key-value存储放在一起并用array数组来存储，`hashMap`通过key存放和查找，时间复杂度o(1)常量，为了更好理解`hashMap`原理，需要明白存储和查找元素，最后`HashMap`也会在面试中问到，因此为了准备面试或者正在准备面试都需要了解

## 2.`put()`方法

存储值在`HashMap`中，调用`put`方法需要传递两个参数，key和value:



```java
V put(K key, V value) 
```

当一个值添加到`map`通过key，key`hashCode()`方法被调用并放回初始hash值，为了更好理解本节内容，

创建一个对象作为key，对象只有一个属性，使用hash code方法返回该属性：

```java
public class MyKey {
    private int id;
    
    @Override
    public int hashCode() {
        System.out.println("Calling hashCode()");
        return id;
    }
 
    // constructor, setters and getters 
}
```

下面例子：

```java
@Test
public void whenHashCodeIsCalledOnPut_thenCorrect() {
    MyKey key = new MyKey(1);
    Map<MyKey, String> map = new HashMap();
    map.put(key, "val");
}
```

上面没有什么输出，但是注意控制台输出 `hashCcode()`确实被调用

```java
Calling hashCode()
```

接下来，`HashMap` 中`hash()`方法计算哈希值使用key初始哈希值，最后哈希值是内部数组中index，或者是篮子中位置，`hash`方法如下：

```java
static final int hash(Object key) {
  int h;
  return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

注意这个地方使用哈希值从key对象计算出哈希值，内部实现方法，如下:

```java
 public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
  }
```

上面方法内部实现调用`putVal`方法传入key哈希值作为第一个参数，一个可能疑问为什么计算哈希值还需要传入key，原因是`HashMap`都需要保存key和value封装一个Map.Entry对象。

上问中讲到，java集合框架继承`Collection`接口但是`Map`不需要，比较`Map`和`Set`接口

```java
public interface Set<E> extends Collection<E> 
```

`HashMap`不存放单个元素而是存放键值对元素，因此`Collection`接口不适合`Map`，下面特殊情况`HashMap`保存空key和空值：

```java
@Test
public void givenNullKeyAndVal_whenAccepts_thenCorrect(){
    Map<String, String> map = new HashMap();
    map.put(null, null);
}
```

当空key进行put操作，哈希值是零，意味在数组中第一个元素，也意味着不需要进行key哈希操作`hashCode`方法不会调用，避免空指针异常，在put操作时，key已经存放，方法返回之前值：

```java
@Test
public void givenExistingKey_whenPutReturnsPrevValue_thenCorrect() {
    Map<String, String> map = new HashMap();
    map.put("key1", "val1");
 
    String rtnVal = map.put("key1", "val2");
 
    assertEquals("val1", rtnVal);
}
```

否则返回为空

```java
@Test
public void givenNewKey_whenPutReturnsNull_thenCorrect() {
    Map<String, String> map = new HashMap();
 
    String rtnVal = map.put("key1", "val1");
 
    assertNull(rtnVal);
}
```

当返回为空不一定是添加成功，可能返回之前值是空：

```java
@Test
public void givenNullVal_whenPutReturnsNull_thenCorrect() {
    Map<String, String> map = new HashMap();
 
    String rtnVal = map.put("key1", null);
 
    assertNull(rtnVal);
}
```

用`containsKey`方法可以区别上面情况

## 3.获取元素API

返回已经存放在`HashMap`元素，`HashMap`是按照键来存储，调用`get`方法传入键返回值：

```java
@Test
public void whenGetWorks_thenCorrect() {
    Map<String, String> map = new HashMap();
    map.put("key", "val");
 
    String val = map.get("key");
 
    assertEquals("val", val);
}
```

哈希方法也会被调用得到初始哈希值，定位到数组位置：

```java
@Test
public void whenHashCodeIsCalledOnGet_thenCorrect() {
    MyKey key = new MyKey(1);
    Map<MyKey, String> map = new HashMap();
    map.put(key, "val");
    map.get(key);
}
```

这个时候`hashCode()`会被调用两次，一次put另外一次get

```java
Calling hashCode()
Calling hashCode()
```

调用`hash`方法内部会重新哈希一次，上面讲解哈希值是数组位置，值存放此位置调用get方法返回值，如果值

返回为空，意味着键没有关联任何值：

```java
@Test
public void givenUnmappedKey_whenGetReturnsNull_thenCorrect() {
    Map<String, String> map = new HashMap();
 
    String rtnVal = map.get("key1");
 
    assertNull(rtnVal);
}
```

或者意味着键的值是空

```java
@Test
public void givenNullVal_whenRetrieves_thenCorrect() {
    Map<String, String> map = new HashMap();
    map.put("key", null);
         
    String val=map.get("key");
         
    assertNull(val);
}
```

为了区别两种场景，使用`containsKey()`，传入键返返回true表示键存在：

```java
@Test
public void whenContainsDistinguishesNullValues_thenCorrect() {
    Map<String, String> map = new HashMap();
 
    String val1 = map.get("key");
    boolean valPresent = map.containsKey("key");
 
    assertNull(val1);
    assertFalse(valPresent);
 
    map.put("key", null);
    String val = map.get("key");
    valPresent = map.containsKey("key");
 
    assertNull(val);
    assertTrue(valPresent);
}
```

上面两种情况进行测试，`get`方法返回值是空，可以区分开上面情况

## 4.集合视图

`HashMap`提供3种视图供我们使用操作键和值作为集合，获取所有键：

```java
public void givenHashMap_whenRetrievesKeyset_thenCorrect() {
    Map<String, String> map = new HashMap();
    map.put("name", "baeldung");
    map.put("type", "blog");
 
    Set<String> keys = map.keySet();
 
    assertEquals(2, keys.size());
    assertTrue(keys.contains("name"));
    assertTrue(keys.contains("type"));
}
```

`set`指向`map`，因此`set`任何改变相应`map`也会改变：

```java
@Test
public void givenKeySet_whenChangeReflectsInMap_thenCorrect() {
    Map<String, String> map = new HashMap();
    map.put("name", "baeldung");
    map.put("type", "blog");
 
    assertEquals(2, map.size());
 
    Set<String> keys = map.keySet();
    keys.remove("name");
 
    assertEquals(1, map.size());
}
```



也可以支持获取所有值：

```java
@Test
public void givenHashMap_whenRetrievesValues_thenCorrect() {
    Map<String, String> map = new HashMap();
    map.put("name", "baeldung");
    map.put("type", "blog");
 
    Collection<String> values = map.values();
 
    assertEquals(2, values.size());
    assertTrue(values.contains("baeldung"));
    assertTrue(values.contains("blog"));
}
```

像上面键`set`一样，`set`中任何改变会影响`map`值，最后得到所有元素：

```java
@Test
public void givenHashMap_whenRetrievesEntries_thenCorrect() {
    Map<String, String> map = new HashMap();
    map.put("name", "baeldung");
    map.put("type", "blog");
 
    Set<Entry<String, String>> entries = map.entrySet();
 
    assertEquals(2, entries.size());
    for (Entry<String, String> e : entries) {
        String key = e.getKey();
        String val = e.getValue();
        assertTrue(key.equals("name") || key.equals("type"));
        assertTrue(val.equals("baeldung") || val.equals("blog"));
    }
}
```

`HashMap`元素是无序，因此排序需要循环所有键和值，许多场景，使用上面最后集合视图，更多是使用迭代器，但是迭代器或出现`fail-fast`，如果迭代器被创建之后，`Map`任何结构被修改，会抛出修改异常：

```java
@Test(expected = ConcurrentModificationException.class)
public void givenIterator_whenFailsFastOnModification_thenCorrect() {
    Map<String, String> map = new HashMap();
    map.put("name", "baeldung");
    map.put("type", "blog");
 
    Set<String> keys = map.keySet();
    Iterator<String> it = keys.iterator();
    map.remove("type");
    while (it.hasNext()) {
        String key = it.next();
    }
}
```

如果要删除元素只有迭代器自己删除：

```java
public void givenIterator_whenRemoveWorks_thenCorrect() {
    Map<String, String> map = new HashMap();
    map.put("name", "baeldung");
    map.put("type", "blog");
 
    Set<String> keys = map.keySet();
    Iterator<String> it = keys.iterator();
 
    while (it.hasNext()) {
        it.next();
        it.remove();
    }
 
    assertEquals(0, map.size());
}
```

最后一点hashMap迭代器集合性能，这点上比`linkedHash`和`treeMap`差点，迭代最差时间复杂度O(n)，n是容量元素总和。

## 5.`HashMap`性能

影响`HashMap`性能有两点：初始化参数容量和负载因子，容量是篮子容大小和数组初始化长度在创建时候，负载因子简单说，`HashMap`添加元素之后容器满了，需要重新扩容，默认容器初始化大小16负载因子0.75，也可以自定初始化值：

```java
Map<String,String> hashMapWithCapacity=new HashMap(32);
Map<String,String> hashMapWithCapacityAndLF=new HashMap(32, 0.5f);
```

初始化设置是java团队调优之后值，如果需要使用自己值也可以，需要明白和理解性能以便于知道怎么设置，

当`hashMap`元素超过生产设置负载因子和容量，需要重新哈希，另外内部数组需要重新创建，有两个数组一个是

初始化，一个是包含所有元素数组。

初始化时候小容量减少空间使用但是增加重新哈希次数，重新哈希是非常昂花销，鉴于如此需要增加初始化容量，

如果容量增加太大，迭代元素时间变长，因此设置高容量迭代时间长，小容量迭代时间短。

## 6.哈希碰撞

哈希碰撞，在这个情况有两个或更多不同键值产生相同哈希值，因此存放在相同位置，这种情况可能存在，因为根据`equals`和`hashCode`来判断，两个不同相等对象可能出现相同哈希值。

也可能因为有限数组大小，因此在扩容之前，小容量哈希碰撞高，java里面`hash`方法中可以解决哈希碰撞，下面提供例子，记住键哈希值决定值存放位置，因此两个键哈希碰撞，它们值存放相同位置，缺省情况是使用链表来实现存储，`put时间复杂度`O(1)和`get`方法时间复杂度O(n)在碰撞情况下，因为查找元素位置是哈希值，同一个数组位置，指向链表每个键值都需要比较`equals`，为了解决碰撞技术，修改键类`hasCode`方法：

```java
public class MyKey {
    private String name;
    private int id;
 
    public MyKey(int id, String name) {
        this.id = id;
        this.name = name;
    }
     
    // standard getters and setters
  
    @Override
    public int hashCode() {
        System.out.println("Calling hashCode()");
        return id;
    } 
  
    // toString override for pretty logging
 
    @Override
    public boolean equals(Object obj) {
        System.out.println("Calling equals() for key: " + obj);
        // generated implementation
    }
 
}
```

`equals`和`hashCode`方法增加日志打印，方便跟踪业务，下面例子:

```java
@Test
public void whenCallsEqualsOnCollision_thenCorrect() {
    HashMap<MyKey, String> map = new HashMap();
    MyKey k1 = new MyKey(1, "firstKey");
    MyKey k2 = new MyKey(2, "secondKey");
    MyKey k3 = new MyKey(2, "thirdKey");
 
    System.out.println("storing value for k1");
    map.put(k1, "firstValue");
    System.out.println("storing value for k2");
    map.put(k2, "secondValue");
    System.out.println("storing value for k3");
    map.put(k3, "thirdValue");
 
    System.out.println("retrieving value for k1");
    String v1 = map.get(k1);
    System.out.println("retrieving value for k2");
    String v2 = map.get(k2);
    System.out.println("retrieving value for k3");
    String v3 = map.get(k3);
 
    assertEquals("firstValue", v1);
    assertEquals("secondValue", v2);
    assertEquals("thirdValue", v3);
}
```



上面测试，创建3中不同键-一个id不同另外两个id相同，因为使用哈希值，相同键会出现碰撞在获取和添加时候，

由于之前碰撞解决方法，我们期望存储和获取值正确，因此在后面3行进行断言，单元测试全部通过，下面是打印日志:

```java
storing value for k1
Calling hashCode()
storing value for k2
Calling hashCode()
storing value for k3
Calling hashCode()
Calling equals() for key: MyKey [name=secondKey, id=2]
retrieving value for k1
Calling hashCode()
retrieving value for k2
Calling hashCode()
retrieving value for k3
Calling hashCode()
Calling equals() for key: MyKey [name=secondKey, id=2]
```

注意存储操作，k1和k2成功映射值通过哈希值，然而k3存储不会如此简单，系统检测已经存在相同k2，调用`equals` 比较区别键在链表中，任何其他键值映射存在相同哈希值和过程和上面一样，如果键哈希值一样，

键不一样，新增元素从链表头结点插入，键值一样则替换当前结点，k3和k2获取值时候用`equals`进行比较

获取正确值。

最后java8中使用链表可以动态使用平衡二叉搜索树当元素超过固定阀值当发生碰撞中，这些改变提高性能，在碰撞情况，获取元素时间复杂度O(logn)。

## 7.总结

本文探索 `map`接口实现者`HashMap` 