package lrn.remote.sys.access.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Path("/drives/")
public class DrivesExplorerService
{
    @Context
    HttpServletRequest servletContext;

    @GET
    @Path("/getlist")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getSystemDrivesList()
    {
	JSONArray jsonObjectList = new JSONArray();

	JSONObject jsonObject = null;

	for (java.nio.file.Path path : FileSystems.getDefault().getRootDirectories())
	{
	    jsonObject = new JSONObject();
	    System.out.println(path.getRoot().toString());
	    try
	    {
		jsonObject.put("Drive", path.getRoot().toString().split(":")[0]);

	    } catch (JSONException e)
	    {

		jsonObject = new JSONObject();
		try
		{
		    jsonObject.put("message", "Error Occured File accessing the system");
		} catch (Exception exp)
		{
		    System.out.println(exp.getMessage());
		}
		jsonObjectList = new JSONArray();
		jsonObjectList.put(jsonObject);

		System.out.println(e.getMessage());
		return jsonObjectList;
	    }
	    jsonObjectList.put(jsonObject);
	}

	return jsonObjectList;
    }

    @GET
    @Path("/getfiles/")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getListOfFilesInDir(@QueryParam("DirName") String DirName) throws Exception
    {
	JSONArray jsonObjectList = new JSONArray();
	JSONObject jsonObject = null;
	File files = new File(DirName + ":\\");

	if (!files.exists())
	{
	    jsonObject = new JSONObject();
	    jsonObject.put("message", "Invalid Directory Name");
	    jsonObjectList.put(jsonObject);
	    return jsonObjectList;
	}
	File dir = null;
	for (File file : files.listFiles())
	{
	    jsonObject = new JSONObject();

	    jsonObject.put("file", file.getAbsolutePath());
	    jsonObjectList.put(jsonObject);
	}
	return jsonObjectList;
    }

    @POST
    @Path("/updatefile/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject updateFile(JSONObject jsonObject) throws Exception
    {
	JSONObject respeObject = null;

	if (jsonObject.isNull("filePath") || jsonObject.isNull("fileContent"))
	{
	    respeObject = new JSONObject();
	    try
	    {
		respeObject.put("Error", "Invalid Parameter");
	    } catch (JSONException e)
	    {
		System.out.println(e.getMessage());
		return jsonObject;
	    }
	}

	OutputStreamWriter outputStreamWriter = null;
	File file = null;
	String message = null;
	try
	{
	    file = new File(jsonObject.getString("filePath"));
	    respeObject = new JSONObject();

	    if (!file.exists())
		file.createNewFile();

	    outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath(), true));

	    outputStreamWriter.write(jsonObject.getString("fileContent"));

	    message = "The file hs been updated successfully";

	} catch (JSONException e)
	{
	    message = "Found JSON exception.";
	    System.out.println(e.getMessage());

	} catch (FileNotFoundException e)
	{
	    message = "File Not Found.";
	    System.out.println(e.getMessage());

	} catch (IOException e)
	{
	    message = "Input output system error.";
	    System.out.println(e.getMessage());
	} finally
	{
	    if (outputStreamWriter != null)
	    {
		try
		{
		    outputStreamWriter.close();
		} catch (IOException e)
		{
		    System.out.println(e.getMessage());
		}
	    }
	}
	respeObject.put("message", message);
	return jsonObject;
    }

    @GET
    @Path("/downloadfile/")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getFileDownloadLink(@QueryParam("filepath") String filePath)
    {
	JSONObject respeObject = null;

	File file = new File(filePath);
	String message = null;

	if (!file.exists())
	{
	    respeObject = new JSONObject();
	    try
	    {
		respeObject.put("Error", "Invalid file path.");

	    } catch (JSONException e)
	    {
		System.out.println(e.getMessage());

	    }
	    return respeObject;
	}

	String path = servletContext.getRequestURL().toString();

	String aContextHttpPath = path.substring(0, path.indexOf("/RemoteSystem/"));

	String httpFileName = "/temp/newFile_" + System.currentTimeMillis() + ".txt";

	String filHttpPath = aContextHttpPath + httpFileName;

	path = path.substring(path.indexOf("/RemoteSystemExpplorar"), path.indexOf("RemoteSystem"));

	message = servletContext.getRealPath(path) + httpFileName;

	java.nio.file.Path temp = null;
	try
	{
	    temp = Files.move(Paths.get(filePath), Paths.get(message));
	} catch (IOException e1)
	{
	    System.out.println("found I/O exception");

	}
	boolean isDone = temp != null ? true : false;

	System.out.println(isDone);

	try
	{
	    respeObject = new JSONObject();
	    respeObject.put("message", filHttpPath);
	} catch (JSONException e)
	{
	    System.out.println(e.getMessage());
	}

	return respeObject;
    }

}
