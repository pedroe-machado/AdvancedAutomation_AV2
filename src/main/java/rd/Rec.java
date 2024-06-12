package rd;

public class Rec {

	public static void main(String[] args) {

		// F1 F3 F5 F6
		// =====>O=====>O=====>O=====>
		// | ^
		// | F2 F4 |
		// ======>O======
		

		double[] y = new double[] { 92.184,	61.005,	10.610,	6.305,	31.443, 212.699};

		double[] v = new double[] { 11.040,	14.743,	8.411,	10.497,	3.555,	28.435};

		double[][] A = new double[][] { { -1, -1, -1, -1, -1, 1 } };

		Reconciliation rec = new Reconciliation(y, v, A);
		System.out.println("Y:");
		rec.printMatrix(y);
		System.out.println("\nV:");
		System.out.println(rec.getStandardDeviation().toString());

		System.out.println("\nY_hat:");
		rec.printMatrix(rec.getReconciledFlow());
		System.out.println("Adjustment:" + rec.getAdjustment());

	}

}
