/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
/**
 * Classe responsable de ..
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.2 $
 */
public class Variable implements net.codjo.variable.Variable, Cloneable {
    private String name;
    private String value;

    public Variable() {}


    public Variable(String name, String value) {
        setName(name);
        setValue(value);
    }

    @Override
    public Object clone() {
        return new Variable(this.name, this.value);
    }


    public void setName(String name) {
        this.name = name;
    }


    public void setValue(String value) {
        this.value = value;
    }


    public String getName() {
        return name;
    }


    public String getValue() {
        return value;
    }


    @Override
    public String toString() {
        return "(" + getName() + " = " + getValue() + ")";
    }
}
