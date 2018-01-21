/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jvminspector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class Node implements TreeNode
{
	private final Node parent_;
	private final List<Node> children_;
	private final String name_;
	
	public Node(Node parent, String name)
	{
		parent_ = parent;
		children_ = new ArrayList<>();
		name_ = name;
		
		if (parent != null)
			parent.add(this);
	}
	
	public boolean isRoot() {
		return getParent()==null;
	}
	
	public String getFullPath() {
		LinkedList<String> parts = new LinkedList<>();
		Node current_node = this;
		while (current_node != null)
		{
			if ( ! current_node.isRoot()) {
				parts.addFirst(current_node.getName());
			}
			current_node = (Node)current_node.getParent();
		}
		return String.join(".", parts);
	}
	
	@Override
	public String toString() {
		if (name_ != null) {
			return getName();
		}
		return "<empty>";
	}
	
	public String getName() {
		return name_;
	}
	
	public List<Node> getChildren() {
		return children_;
	}
	
	public void add(Node child) {
		children_.add(child);
	}
	
	public void sort() {
		children_.sort(new DefaultComparator());
	}
	
	public TreePath getTreePath()
	{
		LinkedList<TreeNode> node_list = new LinkedList<>();
		TreeNode current = this;
		while (current != null) {
			node_list.addFirst(current);
			current = current.getParent();
		}
		return new TreePath(node_list.toArray());
	}
	
	@Override // TreeNode
	public boolean isLeaf()
	{
		return children_.isEmpty();
	}
	@Override // TreeNode
	public boolean getAllowsChildren()
	{
		return true;
	}
	@Override // TreeNode
	public TreeNode getParent() {
		return parent_;
	}
	@Override // TreeNode
	public TreeNode getChildAt(final int index)
	{
		if ((index < 0) || (index >= getChildCount()))
			throw new ArrayIndexOutOfBoundsException("index unmatched");
		return children_.get(index);
	}
	@Override // TreeNode
	public int getChildCount()
	{
		return children_.size();
	}
	@Override // TreeNode
	public int getIndex(TreeNode tn)
	{
		for (int i=0, ni=children_.size(); i<ni; ++i)
		{
			Node node = children_.get(i);
			if (node.equals(tn))
			{
				return i;
			}
		}
		return -1;
	}
	@Override // TreeNode
	public Enumeration children()
	{
		return Collections.enumeration(children_);
	}
	public List<Node> childrenList()
	{
		return children_;
	}
	
	//
	// Tri de la vue de l'arbre du design:
	//	- afficher les modules d'abord, par ordre alphab�tique
	//	- afficher les bus ensuite, par ordre alphabetique
	//	- afficher les fils d'un bus par l'endianness lue dans le VCD
	//
	public static final class DefaultComparator implements Comparator<Node>
	{
		@Override
		public int compare(Node A, Node B)
		{
			return A.getName().compareTo(B.getName());
		}
	}
}