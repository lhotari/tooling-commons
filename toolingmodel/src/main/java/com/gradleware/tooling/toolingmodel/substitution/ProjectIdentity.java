package com.gradleware.tooling.toolingmodel.substitution;

import java.io.Serializable;

public interface ProjectIdentity extends Serializable {

    /**
     * Returns the String that uniquely identifies a project. The de-duped project name would probably do here.
     *
     * @return identifier
     */
    String getId();
}