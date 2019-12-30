/*******************************************************************************
 * Title: CloudSimDisk
 * Description: a module for energy aware storage simulation in CloudSim
 * Author: Baptiste Louis
 * Date: June 2015
 *
 * Address: baptiste_louis@live.fr
 * Source: https://github.com/Udacity2048/CloudSimDisk
 * Website: http://baptistelouis.weebly.com/projects.html
 *
 * Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2015, Lule� University of Technology, Sweden.
 *******************************************************************************/

package org.cloudbus.cloudsimdisk;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.UtilizationModel;

/**
 * My Cloudlet extends Cloudlet.java by adding RequiredFiles and DataFiles parameters. RequiredFiles is a list of
 * fileNames that is required by the Cloudlet. Consequently, these files will be retrieve from the persistent storage of
 * the datacenter. DataFiles is a list of Files that need to be added on the datacenter. Thus, these files will be added
 * to the persistent storage.
 * 
 * @author Baptiste Louis
 * 
 */
public class MyCloudlet extends Cloudlet {

	/** The required files. */
	private List<String>	requiredFiles	= null;

	/** The data files. */
	private List<File>		dataFiles		= null;

	/** The update files. */
	private  List<File>		updateFiles		= null;

	/** The delete files. */
	private  List<String>		deleteFiles 	= null;

	/**
	 * The constructor.
	 * 
	 * @param cloudletId
	 *            id
	 * @param cloudletLength
	 *            length in Million Instruction(s)
	 * @param pesNumber
	 *            number of Processing Element(s)
	 * @param cloudletFileSize
	 *            size in MB
	 * @param cloudletOutputSize
	 *            output size in MB
	 * @param utilizationModelCpu
	 *            CPU model
	 * @param utilizationModelRam
	 *            RAM model
	 * @param utilizationModelBw
	 *            BW model
	 * @param requiredFiles
	 *            list of required filenames
	 * @param dataFiles
	 *            list of "to storage" files
	 */
	public MyCloudlet(int cloudletId, long cloudletLength, int pesNumber, 
			long cloudletFileSize, long cloudletOutputSize, 
			UtilizationModel utilizationModelCpu, 
			UtilizationModel utilizationModelRam, 
			UtilizationModel utilizationModelBw,
			List<String> requiredFiles, List<File> dataFiles) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, 
				utilizationModelCpu, utilizationModelRam, utilizationModelBw);

		setRequiredFiles(requiredFiles);
		setDataFiles(dataFiles);
	}

	// constructor overloading for where all 4 operations(PUT, GET, UPDATE, DELETE) are to be performed
	public MyCloudlet(int cloudletId, long cloudletLength, int pesNumber,
					  long cloudletFileSize, long cloudletOutputSize,
					  UtilizationModel utilizationModelCpu,
					  UtilizationModel utilizationModelRam,
					  UtilizationModel utilizationModelBw,
					  List<String> requiredFiles, List<File> dataFiles, List<File> updateFiles, List<String> deleteFiles) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize,
				utilizationModelCpu, utilizationModelRam, utilizationModelBw);

		setRequiredFiles(requiredFiles);
		setDataFiles(dataFiles);
		setUpdateFiles(updateFiles);
		setDeleteFiles(deleteFiles);
	}

	// GETTER AND SETTER

	/**
	 * Gets the required files.
	 * 
	 * @return the required files
	 */
	@Override
	public List<String> getRequiredFiles() {
		return requiredFiles;
	}

	/**
	 * Sets the required files.
	 * 
	 * @param requiredFiles
	 *            the new required files
	 */
	protected void setRequiredFiles(final List<String> requiredFiles) {
		this.requiredFiles = requiredFiles;
	}

	/**
	 * @return the dataFiles
	 */
	public List<File> getDataFiles() {
		return dataFiles;
	}

	/**
	 * @param dataFiles
	 *            the dataFiles to set
	 */
	public void setDataFiles(List<File> dataFiles) {
		this.dataFiles = dataFiles;
	}

	/**
	 * @return the updateFiles
	 */
	public List<File> getUpdateFiles() {
		return updateFiles;
	}

	/**
	 * @param updateFiles
	 *            the updateFiles to set
	 */
	public void setUpdateFiles(List<File> updateFiles) {
		this.updateFiles = updateFiles;
	}

	/**
	 * @return the deleteFiles
	 */
	public List<String> getDeleteFiles() {
		return deleteFiles;
	}

	/**
	 * @param deleteFiles
	 *            the deleteFiles to set
	 */
	public void setDeleteFiles(List<String> deleteFiles) {
		this.deleteFiles = deleteFiles;
	}


}
