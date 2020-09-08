package com.ibm.research.msr.clustering;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

public class PostProcessClustersTest {

	@Test
	public void removeDuplicateTest() {
		ClusterDetails cd = PowerMockito.mock(ClusterDetails.class);
		List<ClusterDetails> list = new ArrayList<>();
		list.add(cd);
		list.add(cd);
		PostProcessClusters pc = new PostProcessClusters();
		pc.setListofclusters(list);
		assertEquals(2, pc.getListofclusters().size());
		pc.removeDuplicate();
		assertEquals(1, pc.getListofclusters().size());
	}
	
	@Test
	public void combineClustersTest() {
		ClusterDetails cd = PowerMockito.mock(ClusterDetails.class);
		List<ClusterDetails> list = new ArrayList<>();
		list.add(cd);
		PostProcessClusters pc = new PostProcessClusters();
		Map<ClusterDetails, Integer> map = new HashedMap();
		map.put(cd, 1);
		pc.setConsolidatedClusters(map);
		pc.CombineClusters(list);
	}

}
