/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jvminspector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;


public class TreeBuilder {

	public static Node build(String root_name, Set<String> classnames) {
	
		// split package name
		// "top.pack.mod.classname" -> ["top","pack","mod","classname"]
		List<List<String>> classpackages = classnames
				.stream()
				.map(str -> Arrays.asList(str.split("\\.")))
				.collect(Collectors.toList());
		// 
		Node hidden_root_view = new Node(null, root_name);
		Map<String, Node> nodes = new HashMap<>();
		final Dic top_dic = new Dic();
		for (List<String> classpackage : classpackages) {
			Dic dic = top_dic;
			Node parent = hidden_root_view;
			for (String part : classpackage) {
				Dic subdic = dic.get(part);
				Node node = nodes.get(part);
				if (subdic == null) {
					assert (node == null);
					subdic = dic.register(part);
					node = new Node(parent, part);
					nodes.put(part, node);
				} else {
					assert (node != null);
				}
				dic = subdic;
				parent = node;
			}
		}
		return hidden_root_view;
	}

	private static class Dic {

		private final Map<String, Dic> dic_;

		private Dic() {
			dic_ = new TreeMap<>();
		}

		public Dic register(String key) {
			Dic subdic = dic_.get(key);
			if (subdic == null) {
				subdic = new Dic();
				dic_.put(key, subdic);
			}
			return subdic;
		}

		public boolean contains(String name) {
			Dic dic = this.get(name);
			return (dic != null);
		}

		public boolean contains(String[] coordinates) {
			Dic dic = this;
			for (String index : coordinates) {
				dic = dic.get(index);
				if (dic == null) {
					return false;
				}
			}
			return true;
		}

		public Dic get(String key) {
			return dic_.get(key);
		}

		public int size() {
			return dic_.size();
		}
	}
}
