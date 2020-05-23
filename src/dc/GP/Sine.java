package dc.GP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Sine extends AbstractNode implements Cloneable{

	public String label = "Sine";
	public int numChildren =1; 
	public String type = null;
	public int nodeIndex = -1;
	public ArrayList<AbstractNode> children = new  ArrayList<AbstractNode> (1);
	public AbstractNode parent = null;
	@Override
	public Object deepCopy(Object oldObj) throws Exception {
		ObjectOutputStream oos = null;
	      ObjectInputStream ois = null;
	      try
	      {
	         ByteArrayOutputStream bos = 
	               new ByteArrayOutputStream(); // A
	         oos = new ObjectOutputStream(bos); // B
	         // serialize and pass the object
	         oos.writeObject(oldObj);   // C
	         oos.flush();               // D
	         ByteArrayInputStream bin = 
	               new ByteArrayInputStream(bos.toByteArray()); // E
	         ois = new ObjectInputStream(bin);                  // F
	         // return the new object
	         return (Sine) ois.readObject(); // G
	      }
	      catch(Exception e)
	      {
	         System.out.println("Exception in ObjectCloner = " + e);
	         throw(e);
	      }
	      finally
	      {
	         oos.close();
	         ois.close();
	      }
	}

	@Override
	public double eval(double inVal) {
		if (this.children.get(0) != null ){
			double evalValue = Math.sin((this.children.get(0)).eval(inVal));
		//	System.out.println( evalValue );
			if  ( evalValue == Double.MAX_VALUE || evalValue == Double.NEGATIVE_INFINITY ||
					evalValue == Double.POSITIVE_INFINITY|| evalValue ==  Double.NaN)
				return 1000000000.0;
			else
				return evalValue;
		}
		else {
			System.out.println( "left not defined in sine");
			return 1000000000.0;
		}
	}

	@Override
	public String getLabel() {
		
		return label;
	}
	
	@Override
	public Sine clone() {
		AbstractNode sine = new Sine();
		
		sine.perfScore =  this.perfScore;
		((Sine)sine).label = this.label;
		((Sine)sine).numChildren = this.numChildren;
		((Sine)sine).parent =  this.parent;
		((Sine)sine).setNodeIndex(this.nodeIndex);
	        for (int i=0; i<this.numChildren; i++) {
	        	AbstractNode childAdd = this.children.get(i).clone();
	        	childAdd.setNodeIndex(i);
	        	childAdd.setParent(sine);
	        	((Sine)sine).children.add(childAdd);
	    	}
	       // this.children.clear();
	        return ((Sine)sine);
	}
	
	@Override
	public int getNumChildren() {
		
		return numChildren;
	}

	@Override
	public ArrayList<AbstractNode> getChildren() {
		// TODO Auto-generated method stub
		return children;
	}

	@Override
	public double getPerfScore() {
		return perfScore;
	}

	
	@Override
	public void addChild(AbstractNode child) {
		child.setNodeIndex(0);
		child.setParent(this);
		children.add(child);
		
	}
	
	@Override
	public String printAsInFixFunction() {
		if (this.children.get(0) != null ){
			
			String evalValue = "(Math.sin(" + (this.children.get(0)).printAsInFixFunction()+"))";
			return evalValue;
		}
		else {
			System.out.println( "left not defined in Sine");
			return "";
		}
	}
	
	@Override
	public void setParent(AbstractNode parent) {
		this.parent = parent;
	}

	@Override
	public AbstractNode getParent() {
		// TODO Auto-generated method stub
		return parent;
	}

	@Override
	public Sine cloneAndReplaceLeafNode() {
		AbstractNode sine = new Sine();
		
		sine.perfScore =  this.perfScore;
		((Sine)sine).label = this.label;
		((Sine)sine).numChildren = this.numChildren;
		((Sine)sine).parent =  this.parent;
	        for (int i=0; i<this.numChildren; i++) {
	        	AbstractNode childAdd = this.children.get(i).cloneAndReplaceLeafNode();
	        	childAdd.setNodeIndex(i);
	        	childAdd.setParent(sine);
	        	((Sine)sine).children.add(childAdd);
	    	}
	       // this.children.clear();
	        return ((Sine)sine);
	}

	@Override
	public String printAsInFixFunctionSimplify() {
		if (this.children.get(0) != null ){
			String evalValue = "";
			String value = (this.children.get(0)).printAsInFixFunctionSimplify();
			String value_ = value.substring(1, value.length()-1);
			if(isDouble(value_))
				evalValue = "("+String.valueOf(Math.sin(Double.parseDouble(value_)))+")";
			else
				evalValue = "(Math.sin(" + (this.children.get(0)).printAsInFixFunctionSimplify()+"))";
			
			return evalValue;
		}
		else {
			System.out.println( "left not defined in Sine");
			return "";
		}
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "innerNode";
	}
	
	@Override
	public void setNodeIndex(int index) {
		this.nodeIndex = index;
		
	}
	
	@Override
	public int getNodeIndex() {
		return this.nodeIndex;
	}
	
	@Override
	public AbstractNode pruneNode() {
		
			
			AbstractNode node1 =  this.getChildren().get(0);
					
			String nodeType1 = node1.getType();
						
			if (nodeType1 == Const.INNER_NODE_TYPE ){
				AbstractNode child1  = node1.pruneNode();
				
				if ( child1!= null)
				{
					child1.setNodeIndex(0);
					child1.setParent(this);
					this.getChildren().set(0, child1);
				}
				if (isValueNumeric(child1.getLabel()) == true  ){
					double evalValue = Math.sin(valueToNumeric(child1.getLabel()));
					if  ( evalValue == Double.MAX_VALUE || evalValue == Double.NEGATIVE_INFINITY ||
							evalValue == Double.POSITIVE_INFINITY || evalValue ==  Double.NaN)
					{
						return this;
					}
					AbstractNode iterateUpNode = new ConstNode(evalValue);
					iterateUpNode.setNodeIndex(this.getNodeIndex());
					iterateUpNode.setParent(this.getParent());
					//System.out.println("Sin2 return " + evalValue );
					return iterateUpNode;
					
				}
				else
					return this;
				
			}
			else{

				if  (isValueNumeric(node1.getLabel()) == true )
				{
					double nodeValue1 = valueToNumeric(node1.getLabel());
					
					
					AbstractNode parentNode =this.getParent();
					//System.out.println("Sine9 return " + node1.getLabel() +  " this label" + this.getLabel() + " " + node1.getParent() + " " + this.getParent() ) ;
					
					
					if (parentNode == null)
						return this;
					
										
					double evalValue = Math.sin(nodeValue1);
					//System.out.println( evalValue );
					if  ( evalValue == Double.MAX_VALUE || evalValue == Double.NEGATIVE_INFINITY ||
							evalValue == Double.POSITIVE_INFINITY || evalValue ==  Double.NaN)
					{
						return this;
					}
					else
					{
						AbstractNode newNode = new ConstNode(evalValue);
						int index = this.getNodeIndex();
						newNode.setNodeIndex(this.getNodeIndex());
						newNode.setParent(this.getParent());
						//parentNode.getChildren().set(index, newNode); ADesola removed this for the time being
						//System.out.println("sine1 return " + evalValue );
						return newNode;
					}
										
				}
				else
				{
					AbstractNode child1  = node1.pruneNode();
					if ( child1!= null)
					{
						child1.setNodeIndex(0);
						child1.setParent(this);
						this.getChildren().set(0, child1);
					}
					if (isValueNumeric(child1.getLabel()) == true  ){
						double value = valueToNumeric(child1.getLabel()) ;
						
						double evalValue = Math.sin(value);
						if  ( evalValue == Double.MAX_VALUE || evalValue == Double.NEGATIVE_INFINITY ||
								evalValue == Double.POSITIVE_INFINITY || evalValue ==  Double.NaN ||
								Double.toString(evalValue)== "Infinity" ||
								Double.isNaN(evalValue) || 
								Double.isInfinite(evalValue) )
						{
							return this;
						}
						AbstractNode iterateUpNode = new ConstNode(evalValue);
						iterateUpNode.setNodeIndex(this.getNodeIndex());
						iterateUpNode.setParent(this.getParent());
						return iterateUpNode;
						
					}
					return this;
				}	
			}
	}

	@Override
	public double eval(double inVal, double magnitude) {
		if (this.children.get(0) != null ){
			double evalValue = Math.sin((this.children.get(0)).eval(inVal,magnitude));
		//	System.out.println( evalValue );
			if  ( evalValue == Double.MAX_VALUE || evalValue == Double.NEGATIVE_INFINITY ||
					evalValue == Double.POSITIVE_INFINITY|| evalValue ==  Double.NaN)
				return 1000000000.0;
			else
				return evalValue;
		}
		else {
			System.out.println( "left not defined in sine");
			return 1000000000.0;
		}
	}

}
