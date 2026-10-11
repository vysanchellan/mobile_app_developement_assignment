package com.vysan.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Unit tests for conversion to base units within the mass, volume and count families. */
public class UnitConverterTest {

    private static final double DELTA = 1e-9;

    @Test
    public void convertsMassToGrams() {
        assertEquals(100.0, UnitConverter.toBase(0.1, "kg"), DELTA);
        assertEquals(28.3495, UnitConverter.toBase(1, "oz"), DELTA);
        assertEquals(453.592, UnitConverter.toBase(1, "lb"), DELTA);
        assertEquals(30.0, UnitConverter.toBase(30, "g"), DELTA);
    }

    @Test
    public void convertsVolumeToMillilitres() {
        assertEquals(1500.0, UnitConverter.toBase(1.5, "l"), DELTA);
        assertEquals(5.0, UnitConverter.toBase(1, "tsp"), DELTA);
        assertEquals(30.0, UnitConverter.toBase(2, "tbsp"), DELTA);
        assertEquals(240.0, UnitConverter.toBase(1, "cup"), DELTA);
    }

    @Test
    public void countAliasesAllMeanPieces() {
        for (String unit : new String[]{"pcs", "pc", "piece", "pieces", "whole", "clove", "slice"}) {
            assertEquals(unit, UnitConverter.Family.COUNT, UnitConverter.familyOf(unit));
            assertEquals(unit, 2.0, UnitConverter.toBase(2, unit), DELTA);
        }
    }

    @Test
    public void unitLookupIgnoresCaseAndSpaces() {
        assertEquals(UnitConverter.Family.MASS, UnitConverter.familyOf(" KG "));
        assertEquals(1000.0, UnitConverter.toBase(1, "Kg"), DELTA);
    }

    @Test
    public void familiesAreDistinct() {
        assertEquals(UnitConverter.Family.MASS, UnitConverter.familyOf("g"));
        assertEquals(UnitConverter.Family.VOLUME, UnitConverter.familyOf("ml"));
        assertEquals(UnitConverter.Family.COUNT, UnitConverter.familyOf("pcs"));
    }

    @Test
    public void unknownUnitIsReportedNotGuessed() {
        assertFalse(UnitConverter.isKnown("handful"));
        assertNull(UnitConverter.familyOf("handful"));
        assertNull(UnitConverter.familyOf(null));
        assertTrue(UnitConverter.isKnown("tbsp"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void toBaseRejectsUnknownUnit() {
        UnitConverter.toBase(1, "pinch");
    }
}
