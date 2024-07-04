package jadx.plugins.ezxcodegen;

import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class Utils {

	public static boolean isNotEmpty(String s) {
		return s != null && !s.trim().isEmpty();
	}

	public static boolean anyOtherSameNameMethod(MethodNode mth) {
		ClassNode classNode = mth.getParentClass();
		return classNode.getMethods().stream().anyMatch(
				m -> m.getName().equals(mth.getName()) && m != mth
		);
	}

	public static String lowerCaseFirst(String val) {
		if (val == null || val.isEmpty()) return "";
		char[] arr = val.toCharArray();
		arr[0] = Character.toLowerCase(arr[0]);
		return new String(arr);
	}

	public static <T> List<List<T>> getAllCombinations(List<T> arr, int targetSize) {
		List<List<T>> result = new ArrayList<>();
		List<T> combination = new ArrayList<>();
		getCombinations(arr, 0, 0, targetSize, combination, result);
		return result;
	}

	private static <T> void getCombinations(List<T> arr, int start, int index, int targetSize, List<T> combination, List<List<T>> result) {
		if (index == targetSize) {
			result.add(new ArrayList<>(combination));
			return;
		}

		for (int i = start; i < arr.size(); i++) {
			combination.add(arr.get(i));
			getCombinations(arr, i + 1, index + 1, targetSize, combination, result);
			combination.remove(combination.size() - 1);
		}
	}

	public static String getMethodString(MethodNode mth) {
		return mth.getAccessFlags().makeString(true) + mth.getMethodInfo().getShortId();
	}

}
