package ex02.blas;

/*
 * Copyright (C) 2006 Nathaniel Troutman
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contact: Nathaniel Troutman
 * Email: me@nathanieltroutman.net
 */

/**
 * 
 * This is a java implementation of a root finder for quadrics, cubics, and
 * quartics. It is adapted from the following resource.
 * 
 * @author Nathaniel Troutman
 * 
 * START-REFERENCE:: 
 * Roots3And4.c
 * 
 * Used from:
 * http://www.martinreddy.net/ukvrsig/objects/docs/ggems/GemsI/Roots3And4.c
 * 
 * Utility functions to find cubic and quartic roots, coefficients are passed
 * like this:
 * 
 * c[0] + c[1]*x + c[2]*x^2 + c[3]*x^3 + c[4]*x^4 = 0
 * 
 * The functions return the number of non-complex roots and put the values into
 * the s array.
 * 
 * Author: Jochen Schwarze (schwarze@isa.de)
 * 
 * Jan 26, 1990 Version for Graphics Gems Oct 11, 1990 Fixed sign problem for
 * negative q's in SolveQuartic (reported by Mark Podlipec), Old-style function
 * definitions, IsZero() as a macro
 * END-REFERENCE:
 */
public class RootFinder
{
    public static void main( String[] args ) throws Exception
    {
        double[] coeffs = { 1, 5, 3 };
        double[] roots = SolveQuadric( coeffs );

        for ( int i = 0; i < roots.length; i++ )
        {
            System.out.println( i + ":" + roots[i] );
        }
    }

    /* epsilon surrounding for near zero values */
    public static final double EQN_EPS = 1e-9;

    public static final boolean IsZero( double x )
    {
        return ((x) > -EQN_EPS && (x) < EQN_EPS);
    }

    public static double[] SolveQuadric( double[] c )
    {
        // Dim the roots array
        double[] s = new double[0];

        double p, q, D;

        /* normal form: x^2 + px + q = 0 */

        p = c[1] / (2 * c[2]);
        q = c[0] / c[2];

        D = p * p - q;

        if ( IsZero( D ) )
        {
            s = new double[1];
            s[0] = -p;
        }
        else if ( D > 0 )
        {
            double sqrt_D = Math.sqrt( D );
            s = new double[2];
            s[0] = sqrt_D - p;
            s[1] = -sqrt_D - p;
        }

        return s;
    }

    public static double[] SolveCubic( double[] c )
    {
        // Dim the roots array
        double[] s = new double[0];

        int i;
        double sub;
        double A, B, C;
        double sq_A, p, q;
        double cb_p, D;

        /* normal form: x^3 + Ax^2 + Bx + C = 0 */
        A = c[2] / c[3];
        B = c[1] / c[3];
        C = c[0] / c[3];

        /*
         * substitute x = y - A/3 to eliminate quadric term: x^3 +px + q = 0
         */
        sq_A = A * A;
        p = 1.0 / 3 * (-1.0 / 3 * sq_A + B);
        q = 1.0 / 2 * (2.0 / 27 * A * sq_A - 1.0 / 3 * A * B + C);

        /* use Cardano's formula */
        cb_p = p * p * p;
        D = q * q + cb_p;

        if ( IsZero( D ) )
        {
            if ( IsZero( q ) )
            /* one triple solution */
            {
                s = new double[1];
                s[0] = 0;
            }
            else
            /* one single and one double solution */
            {
                double u = Math.cbrt( -q );
                s = new double[2];
                s[0] = 2 * u;
                s[1] = -u;
            }
        }
        else if ( D < 0 )
        /* Casus irreducibilis: three real solutions */
        {
            double phi = 1.0 / 3 * Math.acos( -q / Math.sqrt( -cb_p ) );
            double t = 2 * Math.sqrt( -p );

            s = new double[3];
            s[0] = t * Math.cos( phi );
            s[1] = -t * Math.cos( phi + Math.PI / 3 );
            s[2] = -t * Math.cos( phi - Math.PI / 3 );
        }
        else
        /* one real solution */
        {
            double sqrt_D = Math.sqrt( D );
            double u = Math.cbrt( sqrt_D - q );
            double v = -Math.cbrt( sqrt_D + q );

            s = new double[1];
            s[0] = u + v;
        }

        /* resubstitute */
        sub = 1.0 / 3 * A;

        for ( i = 0; i < s.length; ++i )
            s[i] -= sub;

        return s;
    }

    public static double[] SolveQuartic( double[] c )
    {
        // Dim the roots array
        double[] s = new double[4];

        double[] coeffs = new double[4];
        double z, u, v, sub;
        double A, B, C, D;
        double sq_A, p, q, r;
        int i;

        boolean noRoots = false;

        /* normal form: x^4 + Ax^3 + Bx^2 + Cx + D = 0 */
        A = c[3] / c[4];
        B = c[2] / c[4];
        C = c[1] / c[4];
        D = c[0] / c[4];

        //
        // substitute x = y - A/4 to eliminate cubic term:
        // x^4 + px^2 + qx + r = 0
        //
        sq_A = A * A;
        p = -3.0 / 8 * sq_A + B;
        q = 1.0 / 8 * sq_A * A - 1.0 / 2 * A * B + C;
        r = -3.0 / 256 * sq_A * sq_A + 1.0 / 16 * sq_A * B - 1.0 / 4 * A * C
                + D;

        if ( IsZero( r ) )
        {
            /* no absolute term: y(y^3 + py + q) = 0 */
            coeffs[0] = q;
            coeffs[1] = p;
            coeffs[2] = 0;
            coeffs[3] = 1;

            s = new double[3];
            double[] sTemp = SolveCubic( coeffs );
            
            for (int j = 0; j < sTemp.length; j++) {
				s[j] = sTemp[j];
			}
            s[2] = 0;
        }
        else
        {
            /* solve the resolvent cubic ... */
            coeffs[0] = 1.0 / 2 * r * p - 1.0 / 8 * q * q;
            coeffs[1] = -r;
            coeffs[2] = -1.0 / 2 * p;
            coeffs[3] = 1;

            double[] roots;
            roots = SolveCubic( coeffs );

            /* ... and take the one real solution ... */
            z = roots[0];

            /* ... to build two quadric equations */
            u = z * z - r;
            v = 2 * z - p;

            if ( IsZero( u ) )
                u = 0;
            else if ( u > 0 )
                u = Math.sqrt( u );
            else
                noRoots = true;

            if ( IsZero( v ) )
                v = 0;
            else if ( v > 0 )
                v = Math.sqrt( v );
            else
                noRoots = true;

            if ( !noRoots )
            {
                coeffs[0] = z - u;
                coeffs[1] = q < 0 ? -v : v;
                coeffs[2] = 1;

                roots = SolveQuadric( coeffs );

                coeffs[0] = z + u;
                coeffs[1] = q < 0 ? v : -v;
                coeffs[2] = 1;

                double[] secondQuadric = SolveQuadric( coeffs );

                s = new double[roots.length + secondQuadric.length];
                //if(s == null || s.length == 0) return s;
                for ( i = 0; i < roots.length; i++ )
                {
                    s[i] = roots[i];
                }

                //if(secondQuadric == null || secondQuadric.length == 0) return s;
                for ( i = roots.length; i < s.length; i++ )
                {
                    s[i] = secondQuadric[i - roots.length];
                }
            }
        }

        /* resubstitute */
        if ( !noRoots )
        {
            sub = 1.0 / 4 * A;

            for ( i = 0; i < s.length; ++i )
                s[i] -= sub;
        }

        return s;
    }
}
