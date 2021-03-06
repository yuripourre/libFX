package xml;

import java.io.File;
import java.util.Comparator;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import util.FileUtil;


//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TreeTableView.html
public class XMLFileTree extends TreeTableView<File>
{
	public XMLFileTree(String path)
		{
			super();
			if (path == null) path = ".";
			setRoot(new File(path));
			setupTableColumns();
		}
	
	public void setRoot(File f)
	{
		TreeItem<File> root = createNode(f);  
		root.setExpanded(true);
		setShowRoot(true);
		setRoot(root);
	}
	
	private void setupTableColumns()
	{	
		// --- name column---------------------------------------------------------
		TreeTableColumn<File, String> nameColumn = new TreeTableColumn<File, String>("Name");
		nameColumn.setPrefWidth(220);

		nameColumn.setCellValueFactory(p ->
		{
			File f = p.getValue().getValue();
			String text = f.getParentFile() == null ? "/" : f.getName();

			setOnMouseClicked(ev -> {	if (ev.getClickCount() == 2) 	
			{
				TreeItem<File> tree = getSelectionModel().getSelectedItem();
				if (tree != null)
				{
					File file = tree.getValue();
					if (file != null)	doDoubleClick(file);
				}	
			}
			});
				
			return new ReadOnlyObjectWrapper<String>(text);
		} );

		// --- size column---------------------------------------------------------
		TreeTableColumn<File, File> sizeColumn = new TreeTableColumn<File, File>("Size");
		sizeColumn.setPrefWidth(100);

		sizeColumn.setCellValueFactory( p ->	{	return new ReadOnlyObjectWrapper<File>(p.getValue().getValue());  });
		
		sizeColumn.setCellFactory( p ->
		{
			return new TreeTableCell<File, File>()
			{
				@Override protected void updateItem(File item, boolean empty)
				{
					super.updateItem(item, empty);
					TreeTableView treeTable = p.getTreeTableView();
					TreeItem<File> treeItem = treeTable.getTreeItem(getIndex());

					String txt = null;
					if (item == null || empty || treeItem == null || treeItem.getValue() == null)
						txt = "";
					else if (treeItem.getValue().isDirectory())
						txt = getNChildren(treeItem) + " files";
					else
					{
						if (item.length() > 10000000)
							txt = (item.length() / (1024 * 1024) + " MB");
						else if (item.length() > 10000)
							txt = (item.length() / 1024 + " KB");
						else 		txt = item.length() + " bytes";
					}
					setText(txt);
				}
			};
		});
		sizeColumn.setComparator(new Comparator<File>()
		{
			@Override public int compare(File f1, File f2)
			{
				long s1 = f1.isDirectory() ? 0 : f1.length();
				long s2 = f2.isDirectory() ? 0 : f2.length();
				long result = s1 - s2;
				if (result < 0)		return -1;
				if (result == 0)	return 0;
				return 1;
			}
		});
		getColumns().setAll(nameColumn, sizeColumn);

	}

	
	private void doDoubleClick(File file)
	{
		// TODO pass the call back in for the controller
		System.err.println("pass the call back in for the controller");
	}

	private TreeItem<File> createNode(final File f)
	{
		final TreeItem<File> node = new TreeItem<File>(f)
		{
			private boolean isLeaf;
			private boolean isFirstTimeChildren = true;
			private boolean isFirstTimeLeaf = true;

			@Override public ObservableList<TreeItem<File>> getChildren()
			{
				if (isFirstTimeChildren)
				{
					isFirstTimeChildren = false;
					super.getChildren().setAll(buildChildren(this));
				}
				return super.getChildren();
			}

			@Override public boolean isLeaf()
			{
				if (isFirstTimeLeaf)
				{
					isFirstTimeLeaf = false;
					File f = (File) getValue();
					isLeaf = f.isFile();
				}
				return isLeaf;
			}
		};
		node.setGraphic(new ImageView(FileUtil.getFileIcon(f.getName())));
		return node;
	}

	private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem)
	{
		boolean showAll = false;
		File f = (File) TreeItem.getValue();
		if (f != null && f.isDirectory())
		{
			File[] files = f.listFiles();
			if (files != null)
			{
				ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();
				for (File childFile : files)
					if (showAll || !childFile.isHidden())
						children.add(createNode(childFile));
				return children;
			}
		}

		return FXCollections.emptyObservableList();
	}
	private int getNChildren(TreeItem<File> treeItem)
	{
		if (treeItem == null) return 0;
		File f = (File) treeItem.getValue();
		int sum = 0;
		if (f != null && f.isDirectory())
			for (TreeItem<File> child : treeItem.getChildren())
				sum += getNChildren(child);
		return 1 + sum;
	}

}
