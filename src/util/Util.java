package util;

import java.awt.geom.Point2D;

/**
 * 2次元ベクトル演算を提供するユーティリティクラス。
 * Point2D.Doubleオブジェクトを使用して、ベクトルの加算、減算、正規化、スカラー倍、ノルム計算を行う。
 */
public class Util {

	/**
	 * 2つの点の差を計算する（ベクトルの減算）。
	 *
	 * @param p1 減算元の点
	 * @param p2 減算する点
	 * @return p1 - p2の結果を表す新しい点
	 */
	public static Point2D.Double subtract(Point2D.Double p1, Point2D.Double p2) {
		return new Point2D.Double(p1.x - p2.x, p1.y - p2.y);
	}

	/**
	 * ベクトルを正規化する（単位ベクトルにする）。
	 * ノルムが0の場合は原点を返す。
	 *
	 * @param p 正規化する点（ベクトル）
	 * @return 正規化された単位ベクトル。ノルムが0の場合は(0, 0)
	 */
	public static Point2D.Double normalize(Point2D.Double p) {
		double x = p.x;
		double y = p.y;

		double norm = norm(p);
		if (norm == 0) return new Point2D.Double(0, 0);

		double x_dash = x / norm;
		double y_dash = y / norm;

		return new Point2D.Double(x_dash, y_dash);
	}

	/**
	 * ベクトルをスカラー倍する。
	 *
	 * @param p スカラー倍する点（ベクトル）
	 * @param number 乗算するスカラー値
	 * @return スカラー倍された新しい点
	 */
	public static Point2D.Double multiple(Point2D.Double p, double number) {
		return new Point2D.Double(p.x * number, p.y * number);
	}

	/**
	 * ベクトルのノルム（大きさ）を計算する。
	 * ユークリッド距離として、√(x² + y²)を返す。
	 *
	 * @param p ノルムを計算する点（ベクトル）
	 * @return ベクトルのノルム（大きさ）
	 */
	public static double norm(Point2D.Double p) {
		return Math.sqrt(Math.pow(p.x, 2) + Math.pow(p.y, 2));
	}

	/**
	 * 2つの点を加算する（ベクトルの加算）。
	 *
	 * @param p1 加算する最初の点
	 * @param p2 加算する2番目の点
	 * @return p1 + p2の結果を表す新しい点
	 */
	public static Point2D.Double addition(Point2D.Double p1, Point2D.Double p2) {
		return new Point2D.Double(p1.x + p2.x, p1.y + p2.y);
	}

}
