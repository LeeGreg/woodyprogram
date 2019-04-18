# 基础数据

```java
public class DemoData {

    // id, parentId
    public static List<TreeNode> resData = new ArrayList<>();

    public static List<TreeNode> getOrignalDateTree() {

        TreeNode node1 = new TreeNode("1", "0");
        TreeNode node2 = new TreeNode("2", "1");
        TreeNode node3 = new TreeNode("5", "1");
        TreeNode node4 = new TreeNode("6", "1");
        TreeNode node5 = new TreeNode("3", "2");
        TreeNode node6 = new TreeNode("4", "5");
        TreeNode node7 = new TreeNode("7", "5");
        TreeNode node8 = new TreeNode("8", "6");
        TreeNode node9 = new TreeNode("9", "3");
        TreeNode node10 = new TreeNode("10", "3");
        TreeNode node11 = new TreeNode("11", "3");
        TreeNode node12 = new TreeNode("12", "8");
        TreeNode node13 = new TreeNode("13", "8");
        TreeNode node14 = new TreeNode("14", "8");
        TreeNode node15 = new TreeNode("15", "8");
        TreeNode node16 = new TreeNode("16", "6");

        resData.add(node14);
        resData.add(node15);
        resData.add(node16);
        resData.add(node1);
        resData.add(node2);
        resData.add(node4);
        resData.add(node9);
        resData.add(node10);
        resData.add(node11);
        resData.add(node5);
        resData.add(node6);
        resData.add(node7);
        resData.add(node8);
        resData.add(node12);
        resData.add(node13);
        resData.add(node3);
        
        return resData;
    }
}
```

```java
public class TreeNode {

    private String id;
    private String parentId;
    private List<TreeNode> treeNodeList = new ArrayList<>();
  
    public TreeNode() {
    }
    public TreeNode(String id, String parentId) {
        this.id = id;
        this.parentId = parentId;
    }
  	getter()setter()...
}
```

```java
public class DiGuiTree {

    public static void main(String[] args) {
        List<TreeNode> orignalDateTree = DemoData.getOrignalDateTree();
//        TreeNode diGuiTree = getTree(orignalDateTree, "1");
        TreeNode diGuiTree = getTree(orignalDateTree, "3");

        System.out.println(JSONObject.toJSONString(diGuiTree));

        System.out.println("11111111111111111111111111111111111111");

        List<TreeNode> resultList = new ArrayList<>();
        getListByTree(diGuiTree, resultList);

        System.out.println(JSON.toJSONString(resultList));

        System.out.println("2222222222222222222222222222222222222");

        List<TreeNode> res = new ArrayList<>();
        List<TreeNode> treeUpPartByNodeId = getTreeUpPartByNodeId("11", "1", orignalDateTree, res);

        System.out.println(JSON.toJSONString(treeUpPartByNodeId));
    }

    /**
     * 选择根节点，List构造成树
     *
     * @param orignalDateTree
     * @param rootId
     * @return
     */
    public static TreeNode getTree(List<TreeNode> orignalDateTree, String rootId) {
        Map<String, TreeNode> map = new HashMap<>();
        for (TreeNode node : orignalDateTree) {
            map.put(node.getId(), node);
        }

        TreeNode root = null;
        Iterator<Map.Entry<String, TreeNode>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TreeNode> next = iterator.next();
            TreeNode treeNode = next.getValue();
            if (rootId.equals(treeNode.getId())) {
                root = treeNode;
            } else {
                TreeNode parentNode = map.get(treeNode.getParentId());
                if (parentNode != null) {
                    parentNode.getTreeNodeList().add(treeNode);
                }
            }
        }
        return root;
    }

    /**
     * 通过List，获取自节点到父节点的node
     *
     * @param nodeId
     * @param rootId
     * @param tree
     * @param resultList
     * @return
     */
    public static List<TreeNode> getTreeUpPartByNodeId(String nodeId, String rootId, List<TreeNode> tree, List<TreeNode> resultList) {
        for (TreeNode node : tree) {
            if (nodeId.equals(node.getId())) {
                resultList.add(node);
                if (!rootId.equals(node.getId())) {
                    getTreeUpPartByNodeId(node.getParentId(), rootId, tree, resultList);
                }
            }
        }
        return resultList;
    }

    /**
     * 将树拆成List
     *
     * @param treeNode
     * @param resultList
     * @return
     */
    public static List<TreeNode> getListByTree(TreeNode treeNode, List<TreeNode> resultList) {
        if (treeNode.getTreeNodeList() != null) {
            for (TreeNode t : treeNode.getTreeNodeList()) {
                getListByTree(t, resultList);
                t.setTreeNodeList(null);
            }
        } else {
            resultList.add(treeNode);
        }
        treeNode.setTreeNodeList(null);
        resultList.add(treeNode);
        return resultList;
    }
}
```

