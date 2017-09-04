package edu.utexas.cs.nn.tasks.rlglue.featureextractors.tetris;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rlcommunity.environments.tetris.TetrisState;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.nn.MMNEAT.MMNEAT;
import edu.utexas.cs.nn.evolution.EvolutionaryHistory;
import edu.utexas.cs.nn.parameters.Parameters;

public class RawTetrisStateExtractorTests {

	/**
	 * Instantiates ImageMatchTask
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		MMNEAT.clearClasses();
		EvolutionaryHistory.setInnovation(0);
		EvolutionaryHistory.setHighestGenotypeId(0);
		Parameters.initializeParameterCollections(
				new String[] { "io:false", "netio:false", "allowMultipleFunctions:true", "recurrency:false","splitRawTetrisInputs:false",
						"includeHalfLinearPiecewiseFunction:true", "includeSawtoothFunction:true", "absenceNegative:true" });
		MMNEAT.loadClasses();
	}

	@After
	public void tearDown() throws Exception {
		MMNEAT.clearClasses();
	}
	
	@Test
	public void sanityTest2() {
	System.out.println("--------------------------------------------------------------------");
	System.out.print("raw tetris state extractor sanity check for absenceNegative parameter: ");
	System.out.println(Parameters.parameters.booleanParameter("absenceNegative"));
	System.out.println(Parameters.parameters.booleanParameter("senseHolesDifferently"));
	}
	
	/**
	 * Tests that the correct number of features is returned for a given world state
	 */
	@Test
	public void numFeatures_test() {
		TetrisState testState = new TetrisState(); 
		RawTetrisStateExtractor RTSE = new RawTetrisStateExtractor();
		int first = testState.getHeight() * testState.getWidth() + 1; // extra 1 for bias
		assertEquals(RTSE.numFeatures(), first);
		TetrisState.worldHeight = 90;
		TetrisState.worldWidth = 90;
		int second = 90 * 90 + 1; // 1 for bias
		assertEquals(RTSE.numFeatures(), second);	
		TetrisState.worldHeight = 20;
		TetrisState.worldWidth = 10;
		//Dear future Gab- don't actually change the height and width if you can help it :P
	}
	
	/**
	 * Tests that the current features contain the correct block values and positions (1's and 0's)
	 */
	@Test
	public void extract_test() {
		Parameters.initializeParameterCollections( new String[] { "absenceNegative:false", "io:false", "netio:false" });
		TetrisState testState = new TetrisState(); 
		testState.currentX -= 2;
		testState.currentY -= 1;
		Observation o = testState.get_observation(false);
		RawTetrisStateExtractor RTSE = new RawTetrisStateExtractor();
		double[] first = RTSE.extract(o);
		double[] zeroes = new double[TetrisState.worldHeight * TetrisState.worldWidth];
		for(int i = 0; i < zeroes.length; i++){
			//System.out.println("First at " + i + ": " + first[i]);
			//System.out.println("Ones at " + i + ": " + zeroes[i]);
			assertEquals(first[i], zeroes[i], 0.0);
		}
		// line piece
		testState.worldState[166] = 1;
		testState.worldState[167] = 1;
		testState.worldState[168] = 1;
		testState.worldState[169] = 1;
		// S piece
		testState.worldState[171] = 2;
		testState.worldState[172] = 2;
		testState.worldState[180] = 2;
		testState.worldState[181] = 2;
		// J piece 1
		testState.worldState[192] = 3;
		testState.worldState[193] = 3;
		testState.worldState[194] = 3;
		testState.worldState[182] = 3;
		// J piece 2
		testState.worldState[197] = 4;
		testState.worldState[187] = 4;
		testState.worldState[177] = 4;
		testState.worldState[178] = 4;
		// tri piece
		testState.worldState[195] = 5;
		testState.worldState[185] = 5;
		testState.worldState[175] = 5;
		testState.worldState[186] = 5;
		double[] second = RTSE.extract(testState.get_observation());
		assertEquals(second[4], 0, 0.0);
		assertEquals(second[25], 0, 0.0);
		assertEquals(second[100], 0, 0.0);
		assertEquals(second[150], 0, 0.0);
		assertEquals(second[168], 1, 0.0);
		assertEquals(second[171], 1, 0.0);
		assertEquals(second[184], 0, 0.0);
		assertEquals(second[186], 1, 0.0);
		assertEquals(second[193], 1, 0.0);
		assertEquals(second[197], 1, 0.0);
	}
	
	public void extract_senseHoles_test() {
		Parameters.initializeParameterCollections( new String[] { "senseHolesDifferently:true", "io:false", "netio:false" });
		assertEquals(Parameters.parameters.booleanParameter("absenceNegative"), false);
		TetrisState testState = new TetrisState();
		testState.currentX -= 2;
		testState.currentY -= 1;
		Observation o = testState.get_observation(false);
		RawTetrisStateExtractor RTSE = new RawTetrisStateExtractor();
		double[] first = RTSE.extract(o);
		double[] zeroes = new double[first.length];
		for(int i = 0; i < zeroes.length; i++){//asserts that empty extractor means all zeroes
			zeroes[i] = zeroes[i] - 1; 
			//System.out.println("First at " + i + ": " + first[i]);
			//System.out.println("Ones at " + i + ": " + zeroes[i]);
			assertEquals(first[i], zeroes[i], 0.0);
		}
		
		// line piece
		testState.worldState[166] = 1;
		testState.worldState[167] = 1;
		testState.worldState[168] = 1;
		testState.worldState[169] = 1;
		// S piece
		testState.worldState[171] = 2;
		testState.worldState[172] = 2;
		testState.worldState[180] = 2;
		testState.worldState[181] = 2;
		// J piece 1
		testState.worldState[192] = 3;
		testState.worldState[193] = 3;
		testState.worldState[194] = 3;
		testState.worldState[182] = 3;
		// J piece 2
		testState.worldState[197] = 4;
		testState.worldState[187] = 4;
		testState.worldState[177] = 4;
		testState.worldState[178] = 4;
		// tri piece
		testState.worldState[195] = 5;
		testState.worldState[185] = 5;
		testState.worldState[175] = 5;
		testState.worldState[186] = 5;
		double[] second = RTSE.extract(testState.get_observation());
		assertEquals(second[9], 0, 0.0);
		assertEquals(second[170], 0, 0.0);
		assertEquals(second[172], 1, 0.0);
		assertEquals(second[174], 0, 0.0);
		assertEquals(second[166], 1, 0.0);
		assertEquals(second[190], -1, 0.0);
		assertEquals(second[191], -1, 0.0);
		assertEquals(second[183], 0, 0.0);
		assertEquals(second[176], -1, 0.0);
		assertEquals(second[179], -1, 0.0);
		assertEquals(second[189], -1, 0.0);
		assertEquals(second[188], -1, 0.0);
		assertEquals(second[199], -1, 0.0);
		assertEquals(second[198], -1, 0.0);
		assertEquals(second[186], 1, 0.0);
		assertEquals(second[197], 1, 0.0);
		assertEquals(second[196], -1, 0.0);
	}
	/**
	 * Tests that the current features contain the correct block values and positions (1's and -1's)
	 */
	@Test
	public void extract_negative_test() {
		Parameters.initializeParameterCollections( new String[] { "absenceNegative:true", "io:false", "netio:false"});
		assertEquals(Parameters.parameters.booleanParameter("senseHolesDifferently"), false);
		TetrisState testState = new TetrisState(); 
		testState.currentX -= 2;
		testState.currentY -= 1;
		Observation o = testState.get_observation(false);
		RawTetrisStateExtractor RTSE = new RawTetrisStateExtractor();
		double[] first = RTSE.extract(o);
		double[] zeroes = new double[TetrisState.worldHeight * TetrisState.worldWidth];
		for(int i = 0; i < zeroes.length; i++){
			zeroes[i] = zeroes[i] - 1; 
			System.out.println("First at " + i + ": " + first[i]);
			System.out.println("Ones at " + i + ": " + zeroes[i]);
			assertEquals(first[i], zeroes[i], 0.0);
		}
		// line piece
		testState.worldState[166] = 1;
		testState.worldState[167] = 1;
		testState.worldState[168] = 1;
		testState.worldState[169] = 1;
		// S piece
		testState.worldState[171] = 2;
		testState.worldState[172] = 2;
		testState.worldState[180] = 2;
		testState.worldState[181] = 2;
		// J piece 1
		testState.worldState[192] = 3;
		testState.worldState[193] = 3;
		testState.worldState[194] = 3;
		testState.worldState[182] = 3;
		// J piece 2
		testState.worldState[197] = 4;
		testState.worldState[187] = 4;
		testState.worldState[177] = 4;
		testState.worldState[178] = 4;
		// tri piece
		testState.worldState[195] = 5;
		testState.worldState[185] = 5;
		testState.worldState[175] = 5;
		testState.worldState[186] = 5;
		double[] second = RTSE.extract(testState.get_observation());
		assertEquals(second[4], -1, 0.0);
		assertEquals(second[25], -1, 0.0);
		assertEquals(second[100], -1, 0.0);
		assertEquals(second[150], -1, 0.0);
		assertEquals(second[168], 1, 0.0);
		assertEquals(second[171], 1, 0.0);
		assertEquals(second[184], -1, 0.0);
		assertEquals(second[186], 1, 0.0);
		assertEquals(second[193], 1, 0.0);
		assertEquals(second[197], 1, 0.0);
	}
	
	/**
	 * Tests that the labels given match their coordinates
	 */
	@Test
	public void featureLabels_test() {
		RawTetrisStateExtractor RTSE = new RawTetrisStateExtractor();
		String[] labels = RTSE.featureLabels();
		assertEquals(labels[0], "(0, 0) occupied?");
		assertEquals(labels[199], "(9, 19) occupied?");
		assertEquals(labels[116], "(6, 11) occupied?");
		assertEquals(labels[134], "(4, 13) occupied?");
		//Note: the index if essentially y coordinate * 10 + x coordinate -Gab
	}
	
	/**
	 * Tests that scaling does not actually happen and that the inputs are 1 or 0 anyways
	 */
	@Test
	public void scaleInputs_test() {
		Parameters.initializeParameterCollections( new String[] { "absenceNegative:false" , "io:false", "netio:false"});
		TetrisState testState = new TetrisState(); 
		RawTetrisStateExtractor RTSE = new RawTetrisStateExtractor();
		// line piece
		testState.worldState[166] = 1;
		testState.worldState[167] = 1;
		testState.worldState[168] = 1;
		testState.worldState[169] = 1;
		// S piece
		testState.worldState[171] = 2;
		testState.worldState[172] = 2;
		testState.worldState[180] = 2;
		testState.worldState[181] = 2;
		// J piece 1
		testState.worldState[192] = 3;
		testState.worldState[193] = 3;
		testState.worldState[194] = 3;
		testState.worldState[182] = 3;
		// J piece 2
		testState.worldState[197] = 4;
		testState.worldState[187] = 4;
		testState.worldState[177] = 4;
		testState.worldState[178] = 4;
		// tri piece
		testState.worldState[195] = 5;
		testState.worldState[185] = 5;
		testState.worldState[175] = 5;
		testState.worldState[186] = 5;
		double[] first = RTSE.extract(testState.get_observation());
		assertEquals(first[168], 1, 0.0);
		assertEquals(first[171], 1, 0.0);
		assertEquals(first[190], 0, 0.0);
		assertFalse(first[197] == 4.0);
		assertFalse(first[186] == 5.0);
		double[] scaled = RTSE.scaleInputs(first);
		assertEquals(scaled[168], 1, 0.0);
		assertEquals(scaled[171], 1, 0.0);
		assertEquals(scaled[190], 0, 0.0);
		assertFalse(scaled[197] == 4.0);
		assertFalse(scaled[186] == 5.0);
		assertEquals(first, scaled);
	}
	
	/**
	 * Tests that scaling does not actually happen and that the inputs are 1 or -1 anyways
	 */
	@Test
	public void scaleInputs_negative_test() {
		Parameters.initializeParameterCollections( new String[] { "absenceNegative:true" , "io:false", "netio:false"});
		assertEquals(Parameters.parameters.booleanParameter("senseHolesDifferently"), false);
		TetrisState testState = new TetrisState(); 
		RawTetrisStateExtractor RTSE = new RawTetrisStateExtractor();
		// line piece
		testState.worldState[166] = 1;
		testState.worldState[167] = 1;
		testState.worldState[168] = 1;
		testState.worldState[169] = 1;
		// S piece
		testState.worldState[171] = 2;
		testState.worldState[172] = 2;
		testState.worldState[180] = 2;
		testState.worldState[181] = 2;
		// J piece 1
		testState.worldState[192] = 3;
		testState.worldState[193] = 3;
		testState.worldState[194] = 3;
		testState.worldState[182] = 3;
		// J piece 2
		testState.worldState[197] = 4;
		testState.worldState[187] = 4;
		testState.worldState[177] = 4;
		testState.worldState[178] = 4;
		// tri piece
		testState.worldState[195] = 5;
		testState.worldState[185] = 5;
		testState.worldState[175] = 5;
		testState.worldState[186] = 5;
		double[] first = RTSE.extract(testState.get_observation());
		assertEquals(first[168], 1, 0.0);
		assertEquals(first[171], 1, 0.0);
		assertEquals(first[190], -1, 0.0);
		assertFalse(first[197] == 4.0);
		assertFalse(first[186] == 5.0);
		double[] scaled = RTSE.scaleInputs(first);
		assertEquals(scaled[168], 1, 0.0);
		assertEquals(scaled[171], 1, 0.0);
		assertEquals(scaled[190], -1, 0.0);
		assertFalse(scaled[197] == 4.0);
		assertFalse(scaled[186] == 5.0);
		assertEquals(first, scaled);
	}
}
