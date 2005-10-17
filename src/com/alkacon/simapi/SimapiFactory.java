/*
 * File   : $Source: /alkacon/cvs/AlkaconSimapi/src/com/alkacon/simapi/Attic/SimapiFactory.java,v $
 * Date   : $Date: 2005/10/17 07:35:30 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.alkacon.simapi;

/**
 * Factory to obtain an implementation of the {@link com.alkacon.simapi.Simapi} interface.<p>
 * 
 * @author Alexander Kandzior
 */
public final class SimapiFactory {

    /** The imaging instance to use. */
    private static final Simapi m_instance;

    /**
     * Static initializer to create the default imaging instance.<p>
     */
    static {

        m_instance = new SimapiImpl();
    }

    /**
     * Returns an implementation of the {@link com.alkacon.simapi.Simapi} interface.<p> 
     * 
     * @return an implementation of the {@link com.alkacon.simapi.Simapi} interface
     */
    public static Simapi getInstace() {

        return m_instance;
    }
}