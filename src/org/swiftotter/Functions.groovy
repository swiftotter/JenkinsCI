package org.swiftotter;

class Functions implements Serializable {
    def mergeParameters(ArrayList newParams, ArrayList defaultParams) {
        defaultParams.each { param ->
          println param.toString();
          def value = newParams.findResult {
              return it.getArguments().name == param.getArguments().name ? true : null 
          }
        
          println value.toString();
          if (!value) {
              newParams.addAll([ param ]);
          }
      }

      return newParams
    }
}
