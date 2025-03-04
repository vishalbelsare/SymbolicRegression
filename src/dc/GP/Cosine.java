package dc.GP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Cosine extends AbstractNode implements Cloneable{

	public String label = "Cosine";
	public int numChildren =1; 
	public String type = null;
	public int nodeIndex = -1;
	public ArrayList<AbstractNode> children = new  ArrayList<AbstractNode> (2);
	public AbstractNode parent;
	
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
	         return (Cosine) ois.readObject(); // G
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
			double var1 = (this.children.get(0)).eval(inVal);
			if (Double.isNaN(var1)){
				System.out.println(TreeHelperClass.printTreeToString(this, 0));
					System.out.println("Is NAN");
			}
			
			if  ( inVal == Double.MAX_VALUE || inVal == Double.NEGATIVE_INFINITY ||
					inVal == Double.POSITIVE_INFINITY || inVal ==  Double.NaN ||
					 Double.isInfinite(inVal) || Double.isNaN(inVal) ||
					var1 == Double.MAX_VALUE || var1 == Double.NEGATIVE_INFINITY ||
					var1 == Double.POSITIVE_INFINITY || var1 ==  Double.NaN ||
					 Double.isInfinite(var1) || Double.isNaN(var1))
				return Double.MAX_VALUE;
			
			
			double evalValue = Math.cos(var1);
		//	if (Double.isNaN(evalValue)){	
		//		System.out.println("Is NAN");
		//	}
			
		//	if (Double.isInfinite(evalValue))
		//		System.out.println("Is infinity");
			//System.out.println( evalValue );
			if  ( evalValue == Double.MAX_VALUE || evalValue == Double.NEGATIVE_INFINITY ||
					evalValue == Double.POSITIVE_INFINITY || evalValue ==  Double.NaN ||
					 Double.isInfinite(evalValue)  || Double.isNaN(evalValue))
				return Double.MAX_VALUE;
			else
				return evalValue;
		}
		else {
			System.out.println( "left not defined in Cosine");
			return Double.MAX_VALUE;
		}
	}

	@Override
	public String getLabel() {
		
		return label;
	}
	
	@Override
	public Cosine clone() {
		
		AbstractNode cosine = new Cosine();
		
		cosine.perfScore =  this.perfScore;
		((Cosine)cosine).label = this.label;
		((Cosine)cosine).numChildren = this.numChildren;
		((Cosine)cosine).parent =  this.parent;
		 ((Cosine)cosine).setNodeIndex(this.nodeIndex);
	        for (int i=0; i<this.numChildren; i++) {
	        	
	        	AbstractNode childAdd = this.children.get(i).clone();
	        	childAdd.setNodeIndex(i);
	        	childAdd.setParent(cosine);
	        	((Cosine)cosine).children.add(childAdd);
	    	}
	       // this.children.clear();
	        return ((Cosine)cosine);
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
			
			String evalValue = "(Math.cos(" + (this.children.get(0)).printAsInFixFunction()+"))";
			return evalValue;
		}
		else {
			System.out.println( "left not defined in Cosine");
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
	public Cosine cloneAndReplaceLeafNode() {
		AbstractNode cosine = new Cosine();
		
		cosine.perfScore =  this.perfScore;
		((Cosine)cosine).label = this.label;
		((Cosine)cosine).numChildren = this.numChildren;
		((Cosine)cosine).parent =  this.parent;
	        for (int i=0; i<this.numChildren; i++) {
	        	AbstractNode childAdd = this.children.get(i).cloneAndReplaceLeafNode();
	        	childAdd.setNodeIndex(i);
	        	childAdd.setParent(cosine);
	        	((Cosine)cosine).children.add(childAdd);
	        	
	    	}
	       // this.children.clear();
	        return ((Cosine)cosine);
	}

	@Override
	public String printAsInFixFunctionSimplify() {
	if (this.children.get(0) != null ){
				
			String evalValue = "";
			String value = (this.children.get(0)).printAsInFixFunctionSimplify();
			String value_ = value.substring(1, value.length()-1);
			if(isDouble(value_))
				evalValue = "("+String.valueOf(Math.cos(Double.parseDouble(value_)))+")";
			else
				evalValue = "(Math.cos(" + (this.children.get(0)).printAsInFixFunction()+"))";
			return evalValue;
		}
		else {
			System.out.println( "left not defined in Cosine");
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
					double evalValue = Math.cos(valueToNumeric(child1.getLabel()));
					if  ( evalValue == Double.MAX_VALUE || evalValue == Double.NEGATIVE_INFINITY ||
							evalValue == Double.POSITIVE_INFINITY || evalValue ==  Double.NaN)
					{
						//System.out.println("cosine1 return " + evalValue );
						return this;
					}
					else
					{
						AbstractNode iterateUpNode = new ConstNode(evalValue);
						iterateUpNode.setNodeIndex(this.getNodeIndex());
						iterateUpNode.setParent(this.getParent());
						//this.getParent().getChildren().set(this.getNodeIndex(), iterateUpNode);
						//System.out.println("cosine2 return " + evalValue );
						return iterateUpNode;
					}
				}
				else 
				{
					//System.out.println("cosine3 return " + child1.getLabel() );
					return this;
				}
								
			}
			else{

				if  (isValueNumeric(node1.getLabel()) == true )
				{
					double nodeValue1 = valueToNumeric(node1.getLabel());
					
					//System.out.println("Cosine9 return " + node1.getLabel() +  " this label" + this.getLabel() + " " + node1.getParent() + " " + this.getParent() ) ;
					
					AbstractNode parentNode =this.getParent();
					if (parentNode == null)
						return this;
										
					double evalValue = Math.cos(nodeValue1);
					//System.out.println( evalValue );
					if  ( evalValue == Double.MAX_VALUE || evalValue == Double.NEGATIVE_INFINITY ||
							evalValue == Double.POSITIVE_INFINITY || evalValue ==  Double.NaN)
					{
					//	System.out.println("cosine4 return "  );
						return this;
					}
					else
					{
						AbstractNode newNode = new ConstNode(evalValue);
						newNode.setNodeIndex(this.getNodeIndex());
						newNode.setParent(this.getParent());
						//this.getParent().getChildren().set(this.getNodeIndex(), newNode);
						//System.out.println("cosine5 return "  );
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
						
						double evalValue = Math.cos(value);
						if  ( evalValue == Double.MAX_VALUE || evalValue == Double.NEGATIVE_INFINITY ||
								evalValue == Double.POSITIVE_INFINITY || evalValue ==  Double.NaN)
						{
							//System.out.println("cosine6 return "  );
							return this;
						}
						AbstractNode iterateUpNode = new ConstNode(evalValue);
						iterateUpNode.setNodeIndex(this.getNodeIndex());
						iterateUpNode.setParent(this.getParent());
						//this.getParent().getChildren().set(this.getNodeIndex(), iterateUpNode);
						//System.out.println("cosine10 return "  );
						return iterateUpNode;
						
					}
					//System.out.println("cosine7 return "  );
					return this;
				}	
			}
	}

	@Override
	public double eval(double inVal, double magnitude) {
		if (this.children.get(0) != null ){
			double var1 = (this.children.get(0)).eval(inVal,magnitude);
			if (Double.isNaN(var1)){
				System.out.println(TreeHelperClass.printTreeToString(this, 0));
					System.out.println("Is NAN");
			}
			
			if  ( inVal == Double.MAX_VALUE || inVal == Double.NEGATIVE_INFINITY ||
					inVal == Double.POSITIVE_INFINITY || inVal ==  Double.NaN ||
					Double.compare(inVal, 0.0)  < 0  || Double.isInfinite(inVal) || Double.isNaN(inVal) ||
					var1 == Double.MAX_VALUE || var1 == Double.NEGATIVE_INFINITY ||
					var1 == Double.POSITIVE_INFINITY || var1 ==  Double.NaN ||
					Double.compare(var1, 0.0)  < 0  || Double.isInfinite(var1) || Double.isNaN(var1))
				return Double.MAX_VALUE;
			
			double evalValue = Math.cos(var1);
		
			if  ( evalValue == Double.MAX_VALUE || evalValue == Double.NEGATIVE_INFINITY ||
					evalValue == Double.POSITIVE_INFINITY || evalValue ==  Double.NaN ||
					 Double.isInfinite(evalValue)  || Double.isNaN(evalValue))
				return Double.MAX_VALUE;
			else
				return evalValue;
		}
		else {
			System.out.println( "left not defined in Cosine");
			return Double.MAX_VALUE;
		}
	}
}
